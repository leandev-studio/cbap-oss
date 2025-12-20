package com.cbap.persistence.repository;

import com.cbap.persistence.entity.SavedSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for saved searches.
 */
@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, UUID> {
    
    /**
     * Find all saved searches for a user.
     */
    List<SavedSearch> findByUserUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find saved searches for a user and entity.
     */
    List<SavedSearch> findByUserUserIdAndEntityIdOrderByCreatedAtDesc(UUID userId, String entityId);
}
