package com.cbap.search.service;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for executing search queries against OpenSearch.
 */
@Service
public class SearchQueryService {

    private static final Logger logger = LoggerFactory.getLogger(SearchQueryService.class);

    private final OpenSearchClient openSearchClient;
    private final IndexService indexService;
    private final FilterBuilderService filterBuilderService;

    public SearchQueryService(OpenSearchClient openSearchClient, IndexService indexService, FilterBuilderService filterBuilderService) {
        this.openSearchClient = openSearchClient;
        this.indexService = indexService;
        this.filterBuilderService = filterBuilderService;
    }

    /**
     * Search across all entities or a specific entity.
     * 
     * @param queryText The search query text
     * @param entityId Optional entity ID to limit search to specific entity
     * @param page Page number (0-based)
     * @param size Page size
     * @return Search results
     */
    public SearchResult search(String queryText, String entityId, int page, int size) {
        try {
            List<String> indices = new ArrayList<>();
            
            if (entityId != null && !entityId.isEmpty()) {
                // Search specific entity index
                String indexName = indexService.getIndexName(entityId);
                if (indexService.indexExists(entityId)) {
                    indices.add(indexName);
                } else {
                    // Index doesn't exist, return empty results
                    return new SearchResult(List.of(), 0, page, size);
                }
            } else {
                // Search all entity indices (pattern: cbap-*)
                indices.add("cbap-*");
            }

            // Build query: multi-match across all text fields with improved matching
            Query query;
            String trimmedQuery = queryText.trim();
            
            // Use a combination of multi-match and query_string for flexible matching
            // This helps match "startup" to "Startup Ventures" even if word boundaries are involved
            query = Query.of(q -> q
                    .bool(b -> {
                        var boolBuilder = b
                                .should(s -> s
                                        .multiMatch(mm -> mm
                                                .query(trimmedQuery)
                                                .fields("*")
                                                .fuzziness("AUTO")
                                                .type(org.opensearch.client.opensearch._types.query_dsl.TextQueryType.BestFields)
                                                .operator(org.opensearch.client.opensearch._types.query_dsl.Operator.Or)
                                        )
                                )
                                .should(s -> s
                                        .queryString(qs -> qs
                                                .query("*" + trimmedQuery.toLowerCase() + "*")
                                                .defaultField("*")
                                                .analyzeWildcard(true)
                                        )
                                )
                                .minimumShouldMatch("1")
                                .mustNot(mn -> mn
                                        .term(t -> t
                                                .field("deleted")
                                                .value(org.opensearch.client.opensearch._types.FieldValue.of(true))
                                        )
                                );
                        return boolBuilder;
                    })
            );

            // Build search request
            SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                    .index(indices)
                    .query(query)
                    .from(page * size)
                    .size(size);

            SearchRequest request = requestBuilder.build();
            SearchResponse<Map> response = openSearchClient.search(request, Map.class);

            // Log search details for debugging
            long totalHits = response.hits().total().value();
            logger.info("Search executed: query={}, entityId={}, indices={}, totalHits={}, returnedHits={}", 
                    queryText, entityId, indices, totalHits, response.hits().hits().size());
            
            // Log first few hits for debugging
            if (!response.hits().hits().isEmpty()) {
                logger.info("First {} search hits:", Math.min(3, response.hits().hits().size()));
                for (int i = 0; i < Math.min(3, response.hits().hits().size()); i++) {
                    Hit<Map> hit = response.hits().hits().get(i);
                    Map<String, Object> source = hit.source();
                    logger.info("  Hit {}: entityId={}, recordId={}, companyName={}, name={}", 
                            i, 
                            source.get("entityId"), 
                            source.get("recordId"),
                            source.get("companyName"),
                            source.get("name"));
                }
            } else {
                logger.warn("No search hits found for query: {}", queryText);
            }

            // Extract results
            List<SearchHit> hits = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                Map<String, Object> source = hit.source();
                if (source != null) {
                    String hitEntityId = (String) source.get("entityId");
                    String hitRecordId = (String) source.get("recordId");
                    
                    // Fallback: try to extract from recordId if entityId is missing
                    if (hitEntityId == null && entityId != null && !entityId.isEmpty()) {
                        hitEntityId = entityId;
                    }
                    
                    if (hitRecordId != null) {
                        hits.add(new SearchHit(
                                hitEntityId,
                                hitRecordId,
                                source
                        ));
                    } else {
                        logger.warn("Search hit missing recordId: source={}", source);
                    }
                }
            }
            
            logger.debug("Search results: query={}, hits={}, totalHits={}", queryText, hits.size(), totalHits);

            return new SearchResult(hits, totalHits, page, size);

        } catch (IOException e) {
            logger.error("Error executing search query: query={}, entityId={}", queryText, entityId, e);
            return new SearchResult(List.of(), 0, page, size);
        }
    }

    /**
     * Search with filters for a specific entity.
     * 
     * @param entityId The entity ID
     * @param queryText Optional text query
     * @param filters Filter criteria
     * @param page Page number (0-based)
     * @param size Page size
     * @return Search results
     */
    public SearchResult searchWithFilters(String entityId, String queryText, 
                                          Map<String, Object> filters, int page, int size) {
        try {
            String indexName = indexService.getIndexName(entityId);
            if (!indexService.indexExists(entityId)) {
                return new SearchResult(List.of(), 0, page, size);
            }

            // Build filter query
            Query filterQuery = filterBuilderService.buildFilterQuery(filters);

            // Build final query
            Query finalQuery;
            if (queryText != null && !queryText.trim().isEmpty()) {
                // Combine text search with filters
                finalQuery = Query.of(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .multiMatch(mm -> mm
                                                .query(queryText)
                                                .fields("*")
                                                .fuzziness("AUTO")
                                        )
                                )
                                .must(filterQuery)
                        )
                );
            } else {
                // Use filter query only
                finalQuery = filterQuery;
            }

            // Execute search
            SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                    .index(indexName)
                    .query(finalQuery)
                    .from(page * size)
                    .size(size);

            SearchRequest request = requestBuilder.build();
            SearchResponse<Map> response = openSearchClient.search(request, Map.class);

            // Extract results
            List<SearchHit> hits = new ArrayList<>();
            for (Hit<Map> hit : response.hits().hits()) {
                Map<String, Object> source = hit.source();
                if (source != null) {
                    hits.add(new SearchHit(
                            (String) source.get("entityId"),
                            (String) source.get("recordId"),
                            source
                    ));
                }
            }

            long totalHits = response.hits().total().value();
            return new SearchResult(hits, totalHits, page, size);

        } catch (IOException e) {
            logger.error("Error executing filtered search: entityId={}, query={}", entityId, queryText, e);
            return new SearchResult(List.of(), 0, page, size);
        }
    }

    /**
     * Search result container.
     */
    public static class SearchResult {
        private final List<SearchHit> hits;
        private final long totalHits;
        private final int page;
        private final int size;

        public SearchResult(List<SearchHit> hits, long totalHits, int page, int size) {
            this.hits = hits;
            this.totalHits = totalHits;
            this.page = page;
            this.size = size;
        }

        public List<SearchHit> getHits() {
            return hits;
        }

        public long getTotalHits() {
            return totalHits;
        }

        public int getPage() {
            return page;
        }

        public int getSize() {
            return size;
        }
    }

    /**
     * Search hit container.
     */
    public static class SearchHit {
        private final String entityId;
        private final String recordId;
        private final Map<String, Object> source;

        public SearchHit(String entityId, String recordId, Map<String, Object> source) {
            this.entityId = entityId;
            this.recordId = recordId;
            this.source = source;
        }

        public String getEntityId() {
            return entityId;
        }

        public String getRecordId() {
            return recordId;
        }

        public Map<String, Object> getSource() {
            return source;
        }
    }
}
