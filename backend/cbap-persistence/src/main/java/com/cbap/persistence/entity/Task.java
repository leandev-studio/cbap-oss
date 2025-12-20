package com.cbap.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Task entity.
 * 
 * Represents a task assigned to a user, typically created by workflow transitions.
 */
@Entity
@Table(name = "cbap_tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    @Column(name = "task_id")
    private UUID taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id", nullable = false)
    private EntityDefinition entity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private EntityRecord record;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", nullable = false)
    private User assignee;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.OPEN;

    @Column(name = "priority", length = 20)
    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @Column(name = "due_date")
    private OffsetDateTime dueDate;

    @Column(name = "workflow_state", length = 100)
    private String workflowState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transition_id")
    private WorkflowTransition transition;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")
    private User completedBy;

    @Column(name = "decision", length = 20)
    @Enumerated(EnumType.STRING)
    private TaskDecision decision;

    @Column(name = "decision_comments", columnDefinition = "TEXT")
    private String decisionComments;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", columnDefinition = "jsonb")
    private Map<String, Object> metadataJson;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    public enum TaskStatus {
        OPEN,
        IN_PROGRESS,
        DONE,
        CANCELLED
    }

    public enum TaskPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    public enum TaskDecision {
        APPROVED,
        REJECTED,
        REQUEST_CHANGES
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
        if (status == null) {
            status = TaskStatus.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public OffsetDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(OffsetDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getWorkflowState() {
        return workflowState;
    }

    public void setWorkflowState(String workflowState) {
        this.workflowState = workflowState;
    }

    public WorkflowTransition getTransition() {
        return transition;
    }

    public void setTransition(WorkflowTransition transition) {
        this.transition = transition;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public User getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(User completedBy) {
        this.completedBy = completedBy;
    }

    public TaskDecision getDecision() {
        return decision;
    }

    public void setDecision(TaskDecision decision) {
        this.decision = decision;
    }

    public String getDecisionComments() {
        return decisionComments;
    }

    public void setDecisionComments(String decisionComments) {
        this.decisionComments = decisionComments;
    }

    public Map<String, Object> getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(Map<String, Object> metadataJson) {
        this.metadataJson = metadataJson;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}
