package com.cbap.api.controller;

import com.cbap.api.service.SavedSearchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for saved searches.
 */
@RestController
@RequestMapping("/api/v1/searches")
public class SavedSearchController {

    private final SavedSearchService savedSearchService;

    public SavedSearchController(SavedSearchService savedSearchService) {
        this.savedSearchService = savedSearchService;
    }

    /**
     * Create a saved search.
     * POST /api/v1/searches
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSavedSearch(
            @Valid @RequestBody SavedSearchService.CreateSavedSearchRequest request,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }

        SavedSearchService.SavedSearchDTO savedSearch = savedSearchService.createSavedSearch(request, authentication);

        Map<String, Object> response = new HashMap<>();
        response.put("searchId", savedSearch.getSearchId());
        response.put("entityId", savedSearch.getEntityId());
        response.put("name", savedSearch.getName());
        response.put("description", savedSearch.getDescription());
        response.put("queryText", savedSearch.getQueryText());
        response.put("filters", savedSearch.getFilters());
        response.put("isGlobal", savedSearch.getIsGlobal());
        response.put("createdAt", savedSearch.getCreatedAt());
        response.put("updatedAt", savedSearch.getUpdatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all saved searches for the current user.
     * GET /api/v1/searches
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserSavedSearches(
            @RequestParam(required = false) String entityId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }

        List<SavedSearchService.SavedSearchDTO> searches;
        if (entityId != null && !entityId.isEmpty()) {
            searches = savedSearchService.getEntitySavedSearches(entityId, authentication);
        } else {
            searches = savedSearchService.getUserSavedSearches(authentication);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("searches", searches.stream().map(search -> {
            Map<String, Object> searchMap = new HashMap<>();
            searchMap.put("searchId", search.getSearchId());
            searchMap.put("entityId", search.getEntityId());
            searchMap.put("name", search.getName());
            searchMap.put("description", search.getDescription());
            searchMap.put("queryText", search.getQueryText());
            searchMap.put("filters", search.getFilters());
            searchMap.put("isGlobal", search.getIsGlobal());
            searchMap.put("createdAt", search.getCreatedAt());
            searchMap.put("updatedAt", search.getUpdatedAt());
            return searchMap;
        }).toList());

        return ResponseEntity.ok(response);
    }
}
