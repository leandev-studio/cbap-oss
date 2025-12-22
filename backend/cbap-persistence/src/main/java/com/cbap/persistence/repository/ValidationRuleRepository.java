package com.cbap.persistence.repository;

import com.cbap.persistence.entity.ValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for validation rules.
 */
@Repository
public interface ValidationRuleRepository extends JpaRepository<ValidationRule, UUID> {

    /**
     * Find all validation rules for an entity.
     */
    @Query("SELECT v FROM ValidationRule v WHERE v.entity.entityId = :entityId ORDER BY v.scope, v.propertyName, v.ruleName")
    List<ValidationRule> findByEntityId(@Param("entityId") String entityId);

    /**
     * Find field-level validation rules for a specific property.
     */
    @Query("SELECT v FROM ValidationRule v WHERE v.entity.entityId = :entityId AND v.propertyName = :propertyName AND v.scope = 'FIELD' ORDER BY v.ruleName")
    List<ValidationRule> findByEntityIdAndPropertyName(@Param("entityId") String entityId, @Param("propertyName") String propertyName);

    /**
     * Find entity-level validation rules.
     */
    @Query("SELECT v FROM ValidationRule v WHERE v.entity.entityId = :entityId AND v.scope = 'ENTITY' ORDER BY v.ruleName")
    List<ValidationRule> findEntityLevelRules(@Param("entityId") String entityId);

    /**
     * Find cross-entity validation rules.
     */
    @Query("SELECT v FROM ValidationRule v WHERE v.entity.entityId = :entityId AND v.scope = 'CROSS_ENTITY' ORDER BY v.ruleName")
    List<ValidationRule> findCrossEntityRules(@Param("entityId") String entityId);

    /**
     * Find workflow transition validation rules.
     */
    @Query("SELECT v FROM ValidationRule v WHERE v.entity.entityId = :entityId AND v.scope = 'WORKFLOW_TRANSITION' ORDER BY v.ruleName")
    List<ValidationRule> findWorkflowTransitionRules(@Param("entityId") String entityId);

    /**
     * Find validation rule by ID.
     */
    @Query("SELECT v FROM ValidationRule v WHERE v.validationId = :validationId")
    Optional<ValidationRule> findByValidationId(@Param("validationId") UUID validationId);
}
