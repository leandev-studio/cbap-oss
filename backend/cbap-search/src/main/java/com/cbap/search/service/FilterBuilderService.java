package com.cbap.search.service;

import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.RangeQuery;
import org.opensearch.client.opensearch._types.query_dsl.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for building OpenSearch filter queries from filter criteria.
 */
@Service
public class FilterBuilderService {

    private static final Logger logger = LoggerFactory.getLogger(FilterBuilderService.class);

    /**
     * Build a filter query from filter criteria.
     * 
     * @param filters Map of property names to filter values
     * @return OpenSearch Query
     */
    public Query buildFilterQuery(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            // Return match-all query
            return Query.of(q -> q.matchAll(m -> m));
        }

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String propertyName = entry.getKey();
            Object filterValue = entry.getValue();

            if (filterValue == null) {
                continue;
            }

            // Handle different filter value types
            if (filterValue instanceof Map) {
                // Range filter: {"gte": 100, "lte": 200} or {"from": "2024-01-01", "to": "2024-12-31"}
                @SuppressWarnings("unchecked")
                Map<String, Object> rangeMap = (Map<String, Object>) filterValue;
                RangeQuery.Builder rangeBuilder = new RangeQuery.Builder().field(propertyName);

                if (rangeMap.containsKey("gte")) {
                    rangeBuilder.gte(JsonData.of(rangeMap.get("gte")));
                }
                if (rangeMap.containsKey("lte")) {
                    rangeBuilder.lte(JsonData.of(rangeMap.get("lte")));
                }
                if (rangeMap.containsKey("gt")) {
                    rangeBuilder.gt(JsonData.of(rangeMap.get("gt")));
                }
                if (rangeMap.containsKey("lt")) {
                    rangeBuilder.lt(JsonData.of(rangeMap.get("lt")));
                }
                if (rangeMap.containsKey("from")) {
                    rangeBuilder.gte(JsonData.of(rangeMap.get("from")));
                }
                if (rangeMap.containsKey("to")) {
                    rangeBuilder.lte(JsonData.of(rangeMap.get("to")));
                }

                boolBuilder.must(m -> m.range(rangeBuilder.build()));
            } else if (filterValue instanceof List) {
                // Multi-value filter: ["value1", "value2"]
                @SuppressWarnings("unchecked")
                List<Object> values = (List<Object>) filterValue;
                if (!values.isEmpty()) {
                    BoolQuery.Builder orBuilder = new BoolQuery.Builder();
                    for (Object value : values) {
                        orBuilder.should(s -> s.term(t -> t
                                .field(propertyName)
                                .value(FieldValue.of(value.toString()))
                        ));
                    }
                    boolBuilder.must(m -> m.bool(orBuilder.build()));
                }
            } else {
                // Exact match filter
                boolBuilder.must(m -> m.term(t -> t
                        .field(propertyName)
                        .value(FieldValue.of(filterValue.toString()))
                ));
            }
        }

        // Always exclude deleted records
        boolBuilder.mustNot(mn -> mn.term(t -> t
                .field("deleted")
                .value(FieldValue.of(true))
        ));

        return Query.of(q -> q.bool(boolBuilder.build()));
    }
}
