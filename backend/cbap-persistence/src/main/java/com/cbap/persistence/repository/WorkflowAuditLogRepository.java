package com.cbap.persistence.repository;

import com.cbap.persistence.entity.WorkflowAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for workflow audit logs.
 */
@Repository
public interface WorkflowAuditLogRepository extends JpaRepository<WorkflowAuditLog, UUID> {

    /**
     * Find audit logs for a specific record.
     */
    @Query("SELECT a FROM WorkflowAuditLog a WHERE a.record.recordId = :recordId ORDER BY a.performedAt DESC")
    List<WorkflowAuditLog> findByRecordId(@Param("recordId") UUID recordId);

    /**
     * Find audit logs for a specific record with pagination.
     */
    @Query("SELECT a FROM WorkflowAuditLog a WHERE a.record.recordId = :recordId ORDER BY a.performedAt DESC")
    Page<WorkflowAuditLog> findByRecordId(@Param("recordId") UUID recordId, Pageable pageable);

    /**
     * Find audit logs for an entity.
     */
    @Query("SELECT a FROM WorkflowAuditLog a WHERE a.entity.entityId = :entityId ORDER BY a.performedAt DESC")
    Page<WorkflowAuditLog> findByEntityId(@Param("entityId") String entityId, Pageable pageable);

    /**
     * Find audit logs for a workflow.
     */
    @Query("SELECT a FROM WorkflowAuditLog a WHERE a.workflow.workflowId = :workflowId ORDER BY a.performedAt DESC")
    Page<WorkflowAuditLog> findByWorkflowId(@Param("workflowId") String workflowId, Pageable pageable);
}
