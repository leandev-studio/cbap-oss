package com.cbap.persistence.repository;

import com.cbap.persistence.entity.EntityDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for entity definitions.
 */
@Repository
public interface EntityDefinitionRepository extends JpaRepository<EntityDefinition, String> {

    /**
     * Find entity by name.
     */
    Optional<EntityDefinition> findByName(String name);

    /**
     * Find all entities with their properties loaded.
     */
    @Query("SELECT DISTINCT e FROM EntityDefinition e LEFT JOIN FETCH e.properties ORDER BY e.name ASC")
    List<EntityDefinition> findAllWithProperties();

    /**
     * Find entity by ID with properties loaded.
     */
    @Query("SELECT DISTINCT e FROM EntityDefinition e LEFT JOIN FETCH e.properties WHERE e.entityId = :entityId")
    Optional<EntityDefinition> findByEntityIdWithProperties(@Param("entityId") String entityId);

    /**
     * Check if entity exists by ID.
     */
    boolean existsByEntityId(String entityId);
}
