package com.cbap.persistence.repository;

import com.cbap.persistence.entity.PropertyDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for property definitions.
 */
@Repository
public interface PropertyDefinitionRepository extends JpaRepository<PropertyDefinition, UUID> {

    /**
     * Find all properties for an entity.
     */
    @Query("SELECT p FROM PropertyDefinition p WHERE p.entity.entityId = :entityId ORDER BY p.propertyName ASC")
    List<PropertyDefinition> findByEntityId(@Param("entityId") String entityId);

    /**
     * Find property by entity ID and property name.
     */
    @Query("SELECT p FROM PropertyDefinition p WHERE p.entity.entityId = :entityId AND p.propertyName = :propertyName")
    Optional<PropertyDefinition> findByEntityIdAndPropertyName(
            @Param("entityId") String entityId,
            @Param("propertyName") String propertyName);

    /**
     * Check if property exists for an entity.
     */
    @Query("SELECT COUNT(p) > 0 FROM PropertyDefinition p WHERE p.entity.entityId = :entityId AND p.propertyName = :propertyName")
    boolean existsByEntityIdAndPropertyName(@Param("entityId") String entityId, @Param("propertyName") String propertyName);
}
