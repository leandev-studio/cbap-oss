package com.cbap.search.service;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * Service for managing OpenSearch indices.
 */
@Service
public class IndexService {

    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);

    private final OpenSearchClient openSearchClient;

    public IndexService(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    /**
     * Get the index name for an entity.
     */
    public String getIndexName(String entityId) {
        return "cbap-" + entityId.toLowerCase();
    }

    /**
     * Check if an index exists.
     */
    public boolean indexExists(String entityId) {
        try {
            String indexName = getIndexName(entityId);
            ExistsRequest request = new ExistsRequest.Builder()
                    .index(indexName)
                    .build();
            return openSearchClient.indices().exists(request).value();
        } catch (IOException e) {
            logger.error("Error checking if index exists: {}", entityId, e);
            return false;
        }
    }

    /**
     * Create an index for an entity with appropriate mappings.
     */
    public void createIndex(String entityId) throws IOException {
        String indexName = getIndexName(entityId);

        // Check if index already exists
        if (indexExists(entityId)) {
            logger.debug("Index already exists: {}", indexName);
            return;
        }

        // Create index with dynamic mapping and settings
        CreateIndexRequest request = new CreateIndexRequest.Builder()
                .index(indexName)
                .settings(new IndexSettings.Builder()
                        .numberOfShards("1")
                        .numberOfReplicas("0")
                        .build())
                .mappings(m -> m
                        .dynamic(org.opensearch.client.opensearch._types.mapping.DynamicMapping.True)
                        .properties(Map.of()) // Start with empty properties, let OpenSearch map dynamically
                )
                .build();

        openSearchClient.indices().create(request);
        logger.info("Created OpenSearch index: {}", indexName);
    }

    /**
     * Delete an index (for cleanup/testing).
     */
    public void deleteIndex(String entityId) throws IOException {
        String indexName = getIndexName(entityId);
        if (indexExists(entityId)) {
            openSearchClient.indices().delete(d -> d.index(indexName));
            logger.info("Deleted OpenSearch index: {}", indexName);
        }
    }
}
