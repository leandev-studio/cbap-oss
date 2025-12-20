package com.cbap.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Workflow audit log entry.
 * 
 * Records all workflow state transitions for audit purposes.
 */
@Entity
@Table(name = "cbap_workflow_audit_log")
public class WorkflowAuditLog {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    @Column(name = "audit_id")
    private UUID auditId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id", nullable = false)
    private EntityDefinition entity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private EntityRecord record;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowDefinition workflow;

    @Column(name = "from_state", length = 100)
    private String fromState;

    @Column(name = "to_state", nullable = false, length = 100)
    private String toState;

    @Column(name = "transition_id")
    private UUID transitionId;

    @Column(name = "transition_label", length = 255)
    private String transitionLabel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;

    @Column(name = "performed_at", nullable = false, updatable = false)
    private OffsetDateTime performedAt;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", columnDefinition = "jsonb")
    private Map<String, Object> metadataJson;

    @PrePersist
    protected void onCreate() {
        if (performedAt == null) {
            performedAt = OffsetDateTime.now();
        }
    }

    // Getters and Setters
    public UUID getAuditId() {
        return auditId;
    }

    public void setAuditId(UUID auditId) {
        this.auditId = auditId;
    }

    public EntityDefinition getEntity() {
        return entity;
    }

    public void setEntity(EntityDefinition entity) {
        this.entity = entity;
    }

    public EntityRecord getRecord() {
        return record;
    }

    public void setRecord(EntityRecord record) {
        this.record = record;
    }

    public WorkflowDefinition getWorkflow() {
        return workflow;
    }

    public void setWorkflow(WorkflowDefinition workflow) {
        this.workflow = workflow;
    }

    public String getFromState() {
        return fromState;
    }

    public void setFromState(String fromState) {
        this.fromState = fromState;
    }

    public String getToState() {
        return toState;
    }

    public void setToState(String toState) {
        this.toState = toState;
    }

    public UUID getTransitionId() {
        return transitionId;
    }

    public void setTransitionId(UUID transitionId) {
        this.transitionId = transitionId;
    }

    public String getTransitionLabel() {
        return transitionLabel;
    }

    public void setTransitionLabel(String transitionLabel) {
        this.transitionLabel = transitionLabel;
    }

    public User getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(User performedBy) {
        this.performedBy = performedBy;
    }

    public OffsetDateTime getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(OffsetDateTime performedAt) {
        this.performedAt = performedAt;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Map<String, Object> getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(Map<String, Object> metadataJson) {
        this.metadataJson = metadataJson;
    }
}
