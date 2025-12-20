package com.cbap.api.controller;

import com.cbap.api.service.SearchDisplayService;
import com.cbap.search.service.SearchQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for search operations.
 */
@RestController
@RequestMapping("/api/v1")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    private final SearchQueryService searchQueryService;
    private final SearchDisplayService searchDisplayService;

    public SearchController(
            SearchQueryService searchQueryService,
            SearchDisplayService searchDisplayService) {
        this.searchQueryService = searchQueryService;
        this.searchDisplayService = searchDisplayService;
    }

    /**
     * Global search across entities.
     * GET /api/v1/search?q={query}&entity={entityId}&page={page}&size={size}
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String entity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }

        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Bad Request", "message", "Query parameter 'q' is required"));
        }

        logger.info("Search request received: q={}, entity={}, page={}, size={}", q, entity, page, size);
        
        SearchQueryService.SearchResult result = searchQueryService.search(q, entity, page, size);
        
        logger.info("Search completed: q={}, entity={}, totalHits={}, returnedHits={}", 
                q, entity, result.getTotalHits(), result.getHits().size());

        Map<String, Object> response = new HashMap<>();
        response.put("query", q);
        response.put("entityId", entity);
        response.put("page", result.getPage());
        response.put("size", result.getSize());
        response.put("totalHits", result.getTotalHits());
        response.put("hits", result.getHits().stream().map(hit -> {
            Map<String, Object> hitMap = new HashMap<>();
            hitMap.put("entityId", hit.getEntityId());
            hitMap.put("recordId", hit.getRecordId());
            hitMap.put("data", hit.getSource());
            
            // Compute display value from metadata
            String displayValue = searchDisplayService.computeDisplayValue(
                    hit.getEntityId(), hit.getSource());
            if (displayValue != null) {
                hitMap.put("displayValue", displayValue);
            }
            
            return hitMap;
        }).toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Advanced search with filters for a specific entity.
     * POST /api/v1/entities/{entityId}/records/search
     */
    @PostMapping("/entities/{entityId}/records/search")
    public ResponseEntity<Map<String, Object>> searchWithFilters(
            @PathVariable String entityId,
            @RequestBody(required = false) SearchRequest request,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }

        if (request == null) {
            request = new SearchRequest();
        }

        // Execute search with filters
        try {
            SearchQueryService.SearchResult result = searchQueryService.searchWithFilters(
                    entityId, request.getQuery(), request.getFilters(), 
                    request.getPage() != null ? request.getPage() : 0,
                    request.getSize() != null ? request.getSize() : 20);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("entityId", entityId);
            responseMap.put("query", request.getQuery());
            responseMap.put("filters", request.getFilters());
            responseMap.put("page", result.getPage());
            responseMap.put("size", result.getSize());
            responseMap.put("totalHits", result.getTotalHits());
            responseMap.put("hits", result.getHits().stream().map(hit -> {
                Map<String, Object> hitMap = new HashMap<>();
                hitMap.put("entityId", hit.getEntityId());
                hitMap.put("recordId", hit.getRecordId());
                hitMap.put("data", hit.getSource());
                
                // Compute display value from metadata
                String displayValue = searchDisplayService.computeDisplayValue(
                        hit.getEntityId(), hit.getSource());
                if (displayValue != null) {
                    hitMap.put("displayValue", displayValue);
                }
                
                return hitMap;
            }).toList());

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error", "message", "Search failed: " + e.getMessage()));
        }
    }

    /**
     * Search request DTO.
     */
    public static class SearchRequest {
        private String query;
        private Map<String, Object> filters;
        private Integer page;
        private Integer size;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public Map<String, Object> getFilters() {
            return filters;
        }

        public void setFilters(Map<String, Object> filters) {
            this.filters = filters;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }
    }
}
