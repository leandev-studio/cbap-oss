package com.cbap.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Workflow transition definition.
 * 
 * Represents a transition between two states in a workflow.
 */
@Entity
@Table(name = "cbap_metadata_workflow_transitions")
public class WorkflowTransition {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    @Column(name = "transition_id")
    private UUID transitionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowDefinition workflow;

    @Column(name = "from_state", nullable = false, length = 100)
    private String fromState;

    @Column(name = "to_state", nullable = false, length = 100)
    private String toState;

    @Column(name = "action_label", nullable = false, length = 255)
    private String actionLabel;

    @Column(name = "label_key", length = 255)
    private String labelKey;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conditions_json", columnDefinition = "jsonb")
    private Map<String, Object> conditionsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_roles", columnDefinition = "jsonb")
    private List<String> allowedRoles;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pre_transition_rules", columnDefinition = "jsonb")
    private Map<String, Object> preTransitionRules;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", columnDefinition = "jsonb")
    private Map<String, Object> metadataJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public UUID getTransitionId() {
        return transitionId;
    }

    public void setTransitionId(UUID transitionId) {
        this.transitionId = transitionId;
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

    public String getActionLabel() {
        return actionLabel;
    }

    public void setActionLabel(String actionLabel) {
        this.actionLabel = actionLabel;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getConditionsJson() {
        return conditionsJson;
    }

    public void setConditionsJson(Map<String, Object> conditionsJson) {
        this.conditionsJson = conditionsJson;
    }

    public List<String> getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(List<String> allowedRoles) {
        this.allowedRoles = allowedRoles;
    }

    public Map<String, Object> getPreTransitionRules() {
        return preTransitionRules;
    }

    public void setPreTransitionRules(Map<String, Object> preTransitionRules) {
        this.preTransitionRules = preTransitionRules;
    }

    public Map<String, Object> getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(Map<String, Object> metadataJson) {
        this.metadataJson = metadataJson;
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
}
