package com.cbap.api.service;

import com.cbap.persistence.entity.SavedSearch;
import com.cbap.persistence.entity.User;
import com.cbap.persistence.repository.SavedSearchRepository;
import com.cbap.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing saved searches.
 */
@Service
public class SavedSearchService {

    private static final Logger logger = LoggerFactory.getLogger(SavedSearchService.class);

    private final SavedSearchRepository savedSearchRepository;
    private final UserRepository userRepository;

    public SavedSearchService(SavedSearchRepository savedSearchRepository, UserRepository userRepository) {
        this.savedSearchRepository = savedSearchRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get current user from authentication.
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new IllegalStateException("Authentication required");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }

    /**
     * Create a saved search.
     */
    @Transactional
    public SavedSearchDTO createSavedSearch(CreateSavedSearchRequest request, Authentication authentication) {
        User user = getCurrentUser(authentication);

        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setUser(user);
        savedSearch.setEntityId(request.getEntityId());
        savedSearch.setName(request.getName());
        savedSearch.setDescription(request.getDescription());
        savedSearch.setQueryText(request.getQueryText());
        savedSearch.setFiltersJson(request.getFilters());
        savedSearch.setIsGlobal(request.getIsGlobal() != null ? request.getIsGlobal() : false);

        savedSearch = savedSearchRepository.save(savedSearch);

        logger.info("Saved search created: searchId={}, userId={}, name={}", 
                savedSearch.getSearchId(), user.getUserId(), savedSearch.getName());

        return buildSavedSearchDTO(savedSearch);
    }

    /**
     * Get all saved searches for the current user.
     */
    @Transactional(readOnly = true)
    public List<SavedSearchDTO> getUserSavedSearches(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<SavedSearch> searches = savedSearchRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());
        return searches.stream().map(this::buildSavedSearchDTO).toList();
    }

    /**
     * Get saved searches for a specific entity.
     */
    @Transactional(readOnly = true)
    public List<SavedSearchDTO> getEntitySavedSearches(String entityId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<SavedSearch> searches = savedSearchRepository.findByUserUserIdAndEntityIdOrderByCreatedAtDesc(
                user.getUserId(), entityId);
        return searches.stream().map(this::buildSavedSearchDTO).toList();
    }

    /**
     * Build DTO from entity.
     */
    private SavedSearchDTO buildSavedSearchDTO(SavedSearch savedSearch) {
        SavedSearchDTO dto = new SavedSearchDTO();
        dto.setSearchId(savedSearch.getSearchId());
        dto.setEntityId(savedSearch.getEntityId());
        dto.setName(savedSearch.getName());
        dto.setDescription(savedSearch.getDescription());
        dto.setQueryText(savedSearch.getQueryText());
        dto.setFilters(savedSearch.getFiltersJson());
        dto.setIsGlobal(savedSearch.getIsGlobal());
        dto.setCreatedAt(savedSearch.getCreatedAt());
        dto.setUpdatedAt(savedSearch.getUpdatedAt());
        return dto;
    }

    /**
     * Create saved search request DTO.
     */
    public static class CreateSavedSearchRequest {
        private String entityId;
        private String name;
        private String description;
        private String queryText;
        private Map<String, Object> filters;
        private Boolean isGlobal;

        public String getEntityId() {
            return entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getQueryText() {
            return queryText;
        }

        public void setQueryText(String queryText) {
            this.queryText = queryText;
        }

        public Map<String, Object> getFilters() {
            return filters;
        }

        public void setFilters(Map<String, Object> filters) {
            this.filters = filters;
        }

        public Boolean getIsGlobal() {
            return isGlobal;
        }

        public void setIsGlobal(Boolean isGlobal) {
            this.isGlobal = isGlobal;
        }
    }

    /**
     * Saved search DTO.
     */
    public static class SavedSearchDTO {
        private UUID searchId;
        private String entityId;
        private String name;
        private String description;
        private String queryText;
        private Map<String, Object> filters;
        private Boolean isGlobal;
        private java.time.OffsetDateTime createdAt;
        private java.time.OffsetDateTime updatedAt;

        // Getters and setters
        public UUID getSearchId() {
            return searchId;
        }

        public void setSearchId(UUID searchId) {
            this.searchId = searchId;
        }

        public String getEntityId() {
            return entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getQueryText() {
            return queryText;
        }

        public void setQueryText(String queryText) {
            this.queryText = queryText;
        }

        public Map<String, Object> getFilters() {
            return filters;
        }

        public void setFilters(Map<String, Object> filters) {
            this.filters = filters;
        }

        public Boolean getIsGlobal() {
            return isGlobal;
        }

        public void setIsGlobal(Boolean isGlobal) {
            this.isGlobal = isGlobal;
        }

        public java.time.OffsetDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(java.time.OffsetDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public java.time.OffsetDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(java.time.OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
