package com.cbap.persistence.repository;

import com.cbap.persistence.entity.EntityRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for entity records.
 */
@Repository
public interface EntityRecordRepository extends JpaRepository<EntityRecord, UUID> {

    /**
     * Find all records for an entity (non-deleted only).
     */
    @Query("SELECT r FROM EntityRecord r WHERE r.entity.entityId = :entityId AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    Page<EntityRecord> findByEntityId(@Param("entityId") String entityId, Pageable pageable);

    /**
     * Find record by ID (non-deleted only).
     */
    @Query("SELECT r FROM EntityRecord r WHERE r.recordId = :recordId AND r.deletedAt IS NULL")
    Optional<EntityRecord> findByRecordId(@Param("recordId") UUID recordId);

    /**
     * Find record by entity ID and record ID (non-deleted only).
     */
    @Query("SELECT r FROM EntityRecord r WHERE r.entity.entityId = :entityId AND r.recordId = :recordId AND r.deletedAt IS NULL")
    Optional<EntityRecord> findByEntityIdAndRecordId(
            @Param("entityId") String entityId,
            @Param("recordId") UUID recordId);

    /**
     * Count records for an entity (non-deleted only).
     */
    @Query("SELECT COUNT(r) FROM EntityRecord r WHERE r.entity.entityId = :entityId AND r.deletedAt IS NULL")
    long countByEntityId(@Param("entityId") String entityId);
}
