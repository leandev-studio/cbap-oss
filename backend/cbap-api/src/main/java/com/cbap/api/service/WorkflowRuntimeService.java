package com.cbap.api.service;

import com.cbap.persistence.entity.*;
import com.cbap.persistence.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Hibernate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for executing workflow transitions.
 */
@Service
public class WorkflowRuntimeService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowRuntimeService.class);

    private final EntityDefinitionRepository entityDefinitionRepository;
    private final EntityRecordRepository entityRecordRepository;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowStateRepository workflowStateRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final WorkflowAuditLogRepository workflowAuditLogRepository;
    private final UserRepository userRepository;
    private final com.cbap.persistence.repository.TaskRepository taskRepository;
    private final ValidationService validationService;

    public WorkflowRuntimeService(
            EntityDefinitionRepository entityDefinitionRepository,
            EntityRecordRepository entityRecordRepository,
            WorkflowDefinitionRepository workflowDefinitionRepository,
            WorkflowStateRepository workflowStateRepository,
            WorkflowTransitionRepository workflowTransitionRepository,
            WorkflowAuditLogRepository workflowAuditLogRepository,
            UserRepository userRepository,
            com.cbap.persistence.repository.TaskRepository taskRepository,
            ValidationService validationService) {
        this.entityDefinitionRepository = entityDefinitionRepository;
        this.entityRecordRepository = entityRecordRepository;
        this.workflowDefinitionRepository = workflowDefinitionRepository;
        this.workflowStateRepository = workflowStateRepository;
        this.workflowTransitionRepository = workflowTransitionRepository;
        this.workflowAuditLogRepository = workflowAuditLogRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.validationService = validationService;
    }

    /**
     * Execute a workflow transition.
     * 
     * @param entityId The entity ID
     * @param recordId The record ID
     * @param transitionId The transition ID to execute
     * @param comments Optional comments for the transition
     * @param authentication The authentication context
     * @return Transition result
     */
    @Transactional
    public TransitionResult executeTransition(
            String entityId,
            UUID recordId,
            UUID transitionId,
            String comments,
            Authentication authentication) {

        // Get current user
        User user = getCurrentUser(authentication);

        // Get entity definition
        EntityDefinition entity = entityDefinitionRepository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityId));

        // Verify entity has a workflow
        if (entity.getWorkflowId() == null || entity.getWorkflowId().isEmpty()) {
            throw new IllegalStateException("Entity does not have a workflow assigned");
        }

        // Get record
        EntityRecord record = entityRecordRepository.findByEntityIdAndRecordId(entityId, recordId)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + recordId));

        // Get workflow (fetch separately to avoid MultipleBagFetchException)
        WorkflowDefinition workflow = workflowDefinitionRepository
                .findById(entity.getWorkflowId())
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + entity.getWorkflowId()));
        
        // Initialize lazy collections using Hibernate.initialize() to avoid MultipleBagFetchException
        // This is safe even with orphanRemoval=true because we're not replacing the collection
        Hibernate.initialize(workflow.getStates());
        Hibernate.initialize(workflow.getTransitions());

        // Get transition
        WorkflowTransition transition = workflowTransitionRepository
                .findByTransitionId(transitionId)
                .orElseThrow(() -> new IllegalArgumentException("Transition not found: " + transitionId));

        // Verify transition belongs to the workflow
        if (!transition.getWorkflow().getWorkflowId().equals(workflow.getWorkflowId())) {
            throw new IllegalArgumentException("Transition does not belong to the entity's workflow");
        }

        // Get current state
        String currentState = record.getState();
        if (currentState == null || currentState.isEmpty()) {
            // If no state, set to initial state
            currentState = workflow.getInitialState();
            record.setState(currentState);
            record.setUpdatedBy(user);
            record = entityRecordRepository.save(record);
        }

        // Validate transition is valid from current state
        if (!transition.getFromState().equals(currentState)) {
            throw new IllegalStateException(
                    String.format("Cannot execute transition from state '%s'. Current state is '%s'", 
                            transition.getFromState(), currentState));
        }

        // Validate user has permission (check allowed roles)
        if (transition.getAllowedRoles() != null && !transition.getAllowedRoles().isEmpty()) {
            // TODO: Check if user has one of the allowed roles
            // For now, we'll skip this check - it should be implemented with proper role checking
            logger.debug("Transition requires roles: {}", transition.getAllowedRoles());
        }

        // Validate conditions (if any)
        if (transition.getConditionsJson() != null && !transition.getConditionsJson().isEmpty()) {
            // TODO: Evaluate conditions against record data
            // For now, we'll skip condition evaluation - it should be implemented with a rule engine
            logger.debug("Transition has conditions: {}", transition.getConditionsJson());
        }

        // Validate workflow transition rules (before state change)
        Map<String, Object> recordData = record.getDataJson() != null ? record.getDataJson() : new HashMap<>();
        List<ValidationService.ValidationError> validationErrors = validationService.validateRecord(
                entityId, recordData, "TRANSITION", null);
        if (!validationErrors.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation failed: ");
            for (ValidationService.ValidationError error : validationErrors) {
                if (error.getPropertyName() != null) {
                    errorMessage.append(error.getPropertyName()).append(": ");
                }
                errorMessage.append(error.getMessage()).append("; ");
            }
            throw new IllegalStateException(errorMessage.toString());
        }

        // Evaluate pre-transition rules (if any)
        if (transition.getPreTransitionRules() != null && !transition.getPreTransitionRules().isEmpty()) {
            // TODO: Evaluate pre-transition rules
            // For now, we'll skip rule evaluation - it should be implemented with a rule engine
            logger.debug("Transition has pre-transition rules: {}", transition.getPreTransitionRules());
        }

        // Execute transition: update record state
        String previousState = record.getState();
        record.setState(transition.getToState());
        record.setUpdatedBy(user);
        record = entityRecordRepository.save(record);

        // Create audit log entry
        WorkflowAuditLog auditLog = new WorkflowAuditLog();
        auditLog.setEntity(entity);
        auditLog.setRecord(record);
        auditLog.setWorkflow(workflow);
        auditLog.setFromState(previousState);
        auditLog.setToState(transition.getToState());
        auditLog.setTransitionId(transition.getTransitionId()); // UUID is correct
        auditLog.setTransitionLabel(transition.getActionLabel());
        auditLog.setPerformedBy(user);
        auditLog.setComments(comments);
        auditLog.setMetadataJson(new HashMap<>());
        workflowAuditLogRepository.save(auditLog);

        logger.info("Workflow transition executed: entityId={}, recordId={}, fromState={}, toState={}, transitionId={}, userId={}",
                entityId, recordId, previousState, transition.getToState(), transitionId, user.getUserId());

        // Close tasks associated with this transition
        List<com.cbap.persistence.entity.Task> relatedTasks = taskRepository.findByRecordId(recordId);
        for (com.cbap.persistence.entity.Task task : relatedTasks) {
            // Close tasks that are associated with this transition and are still open
            if (task.getTransition() != null && 
                task.getTransition().getTransitionId().equals(transitionId) &&
                (task.getStatus() == com.cbap.persistence.entity.Task.TaskStatus.OPEN || 
                 task.getStatus() == com.cbap.persistence.entity.Task.TaskStatus.IN_PROGRESS)) {
                task.setStatus(com.cbap.persistence.entity.Task.TaskStatus.DONE);
                task.setCompletedAt(OffsetDateTime.now());
                task.setCompletedBy(user);
                taskRepository.save(task);
                logger.info("Closed task: taskId={}, transitionId={}", task.getTaskId(), transitionId);
            }
        }

        // Create tasks if transition metadata specifies task creation
        if (transition.getMetadataJson() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> tasksConfig = (Map<String, Object>) transition.getMetadataJson().get("createTasks");
            if (tasksConfig != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> tasks = (List<Map<String, Object>>) tasksConfig.get("tasks");
                if (tasks != null) {
                    for (Map<String, Object> taskConfig : tasks) {
                        try {
                            createTaskFromTransition(entityId, recordId, transition, taskConfig, user);
                        } catch (Exception e) {
                            logger.warn("Failed to create task from transition: transitionId={}, error={}",
                                    transitionId, e.getMessage());
                            // Don't fail the transition if task creation fails
                        }
                    }
                }
            }
        }

        // Return result
        TransitionResult result = new TransitionResult();
        result.setEntityId(entityId);
        result.setRecordId(recordId.toString());
        result.setFromState(previousState);
        result.setToState(transition.getToState());
        result.setTransitionId(transitionId.toString());
        result.setTransitionLabel(transition.getActionLabel());
        result.setPerformedBy(user.getUserId().toString());
        result.setPerformedAt(OffsetDateTime.now());
        result.setComments(comments);

        return result;
    }

    /**
     * Get available transitions for a record.
     */
    @Transactional(readOnly = true)
    public List<AvailableTransitionDTO> getAvailableTransitions(String entityId, UUID recordId) {
        // Get entity definition
        EntityDefinition entity = entityDefinitionRepository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityId));

        // Verify entity has a workflow
        if (entity.getWorkflowId() == null || entity.getWorkflowId().isEmpty()) {
            return List.of();
        }

        // Get record
        EntityRecord record = entityRecordRepository.findByEntityIdAndRecordId(entityId, recordId)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + recordId));

        // Get current state
        String currentState = record.getState();
        if (currentState == null) {
            // If no state, get initial state from workflow
            WorkflowDefinition workflow = workflowDefinitionRepository
                    .findById(entity.getWorkflowId())
                    .orElse(null);
            if (workflow != null) {
                currentState = workflow.getInitialState();
            } else {
                return List.of();
            }
        }

        // Get available transitions from current state
        List<WorkflowTransition> transitions = workflowTransitionRepository
                .findByWorkflowIdAndFromState(entity.getWorkflowId(), currentState);

        return transitions.stream()
                .map(t -> {
                    AvailableTransitionDTO dto = new AvailableTransitionDTO();
                    dto.setTransitionId(t.getTransitionId().toString());
                    dto.setFromState(t.getFromState());
                    dto.setToState(t.getToState());
                    dto.setActionLabel(t.getActionLabel());
                    dto.setLabelKey(t.getLabelKey());
                    dto.setDescription(t.getDescription());
                    dto.setAllowedRoles(t.getAllowedRoles());
                    return dto;
                })
                .toList();
    }

    /**
     * Get workflow audit log for a record.
     */
    @Transactional(readOnly = true)
    public List<WorkflowAuditLogDTO> getAuditLog(String entityId, UUID recordId) {
        List<WorkflowAuditLog> auditLogs = workflowAuditLogRepository.findByRecordId(recordId);
        return auditLogs.stream()
                .map(this::auditLogToDTO)
                .toList();
    }

    /**
     * Convert WorkflowAuditLog to DTO.
     */
    private WorkflowAuditLogDTO auditLogToDTO(WorkflowAuditLog auditLog) {
        WorkflowAuditLogDTO dto = new WorkflowAuditLogDTO();
        dto.setAuditId(auditLog.getAuditId().toString());
        dto.setEntityId(auditLog.getEntity().getEntityId());
        dto.setRecordId(auditLog.getRecord().getRecordId().toString());
        dto.setWorkflowId(auditLog.getWorkflow().getWorkflowId());
        dto.setFromState(auditLog.getFromState());
        dto.setToState(auditLog.getToState());
        dto.setTransitionId(auditLog.getTransitionId() != null ? auditLog.getTransitionId().toString() : null);
        dto.setTransitionLabel(auditLog.getTransitionLabel());
        dto.setPerformedBy(auditLog.getPerformedBy().getUserId().toString());
        dto.setPerformedByUsername(auditLog.getPerformedBy().getUsername()); // Add username
        dto.setPerformedAt(auditLog.getPerformedAt());
        dto.setComments(auditLog.getComments());
        dto.setMetadataJson(auditLog.getMetadataJson());
        return dto;
    }

    /**
     * Get current user from authentication.
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Authentication required");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }

    /**
     * Transition Result DTO.
     */
    public static class TransitionResult {
        private String entityId;
        private String recordId;
        private String fromState;
        private String toState;
        private String transitionId;
        private String transitionLabel;
        private String performedBy;
        private OffsetDateTime performedAt;
        private String comments;

        // Getters and Setters
        public String getEntityId() { return entityId; }
        public void setEntityId(String entityId) { this.entityId = entityId; }
        public String getRecordId() { return recordId; }
        public void setRecordId(String recordId) { this.recordId = recordId; }
        public String getFromState() { return fromState; }
        public void setFromState(String fromState) { this.fromState = fromState; }
        public String getToState() { return toState; }
        public void setToState(String toState) { this.toState = toState; }
        public String getTransitionId() { return transitionId; }
        public void setTransitionId(String transitionId) { this.transitionId = transitionId; }
        public String getTransitionLabel() { return transitionLabel; }
        public void setTransitionLabel(String transitionLabel) { this.transitionLabel = transitionLabel; }
        public String getPerformedBy() { return performedBy; }
        public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
        public OffsetDateTime getPerformedAt() { return performedAt; }
        public void setPerformedAt(OffsetDateTime performedAt) { this.performedAt = performedAt; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }

    /**
     * Available Transition DTO.
     */
    public static class AvailableTransitionDTO {
        private String transitionId;
        private String fromState;
        private String toState;
        private String actionLabel;
        private String labelKey;
        private String description;
        private List<String> allowedRoles;

        // Getters and Setters
        public String getTransitionId() { return transitionId; }
        public void setTransitionId(String transitionId) { this.transitionId = transitionId; }
        public String getFromState() { return fromState; }
        public void setFromState(String fromState) { this.fromState = fromState; }
        public String getToState() { return toState; }
        public void setToState(String toState) { this.toState = toState; }
        public String getActionLabel() { return actionLabel; }
        public void setActionLabel(String actionLabel) { this.actionLabel = actionLabel; }
        public String getLabelKey() { return labelKey; }
        public void setLabelKey(String labelKey) { this.labelKey = labelKey; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<String> getAllowedRoles() { return allowedRoles; }
        public void setAllowedRoles(List<String> allowedRoles) { this.allowedRoles = allowedRoles; }
    }

    /**
     * Workflow Audit Log DTO.
     */
    public static class WorkflowAuditLogDTO {
        private String auditId;
        private String entityId;
        private String recordId;
        private String workflowId;
        private String fromState;
        private String toState;
        private String transitionId;
        private String transitionLabel;
        private String performedBy;
        private String performedByUsername;
        private OffsetDateTime performedAt;
        private String comments;
        private java.util.Map<String, Object> metadataJson;

        // Getters and Setters
        public String getAuditId() { return auditId; }
        public void setAuditId(String auditId) { this.auditId = auditId; }
        public String getEntityId() { return entityId; }
        public void setEntityId(String entityId) { this.entityId = entityId; }
        public String getRecordId() { return recordId; }
        public void setRecordId(String recordId) { this.recordId = recordId; }
        public String getWorkflowId() { return workflowId; }
        public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
        public String getFromState() { return fromState; }
        public void setFromState(String fromState) { this.fromState = fromState; }
        public String getToState() { return toState; }
        public void setToState(String toState) { this.toState = toState; }
        public String getTransitionId() { return transitionId; }
        public void setTransitionId(String transitionId) { this.transitionId = transitionId; }
        public String getTransitionLabel() { return transitionLabel; }
        public void setTransitionLabel(String transitionLabel) { this.transitionLabel = transitionLabel; }
        public String getPerformedBy() { return performedBy; }
        public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
        public String getPerformedByUsername() { return performedByUsername; }
        public void setPerformedByUsername(String performedByUsername) { this.performedByUsername = performedByUsername; }
        public OffsetDateTime getPerformedAt() { return performedAt; }
        public void setPerformedAt(OffsetDateTime performedAt) { this.performedAt = performedAt; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
        public java.util.Map<String, Object> getMetadataJson() { return metadataJson; }
        public void setMetadataJson(java.util.Map<String, Object> metadataJson) { this.metadataJson = metadataJson; }
    }

    /**
     * Create a task from workflow transition metadata.
     * 
     * Task config format (in transition.metadata_json.createTasks.tasks[]):
     * {
     *   "assigneeId": "user-uuid",
     *   "title": "Task title",
     *   "description": "Task description",
     *   "priority": "HIGH",
     *   "dueDate": "2025-12-31T23:59:59Z"
     * }
     */
    private void createTaskFromTransition(
            String entityId,
            UUID recordId,
            WorkflowTransition transition,
            Map<String, Object> taskConfig,
            User createdBy) {
        
        try {
            // Extract task configuration
            UUID assigneeId;
            if (taskConfig.get("assigneeId") != null) {
                assigneeId = UUID.fromString(taskConfig.get("assigneeId").toString());
            } else {
                logger.warn("Task config missing assigneeId, skipping task creation");
                return;
            }
            
            String title = taskConfig.getOrDefault("title", transition.getActionLabel()).toString();
            String description = taskConfig.get("description") != null 
                    ? taskConfig.get("description").toString() 
                    : null;
            
            com.cbap.persistence.entity.Task.TaskPriority priority = null;
            if (taskConfig.get("priority") != null) {
                try {
                    priority = com.cbap.persistence.entity.Task.TaskPriority.valueOf(
                            taskConfig.get("priority").toString().toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid priority value: {}", taskConfig.get("priority"));
                }
            }
            
            OffsetDateTime dueDate = null;
            if (taskConfig.get("dueDate") != null) {
                dueDate = OffsetDateTime.parse(taskConfig.get("dueDate").toString());
            }
            
            // Create task directly
            com.cbap.persistence.entity.Task task = new com.cbap.persistence.entity.Task();
            task.setEntity(entityDefinitionRepository.findById(entityId)
                    .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityId)));
            task.setRecord(entityRecordRepository.findByEntityIdAndRecordId(entityId, recordId)
                    .orElseThrow(() -> new IllegalArgumentException("Record not found: " + recordId)));
            task.setTitle(title);
            task.setDescription(description);
            task.setAssignee(userRepository.findById(assigneeId)
                    .orElseThrow(() -> new IllegalArgumentException("Assignee not found: " + assigneeId)));
            task.setStatus(com.cbap.persistence.entity.Task.TaskStatus.OPEN);
            task.setPriority(priority);
            task.setDueDate(dueDate);
            task.setWorkflowState(transition.getToState());
            task.setTransition(transition);
            task.setCreatedBy(createdBy);
            
            taskRepository.save(task);
            
            logger.info("Task created from transition: taskId={}, entityId={}, recordId={}, assigneeId={}, title={}",
                    task.getTaskId(), entityId, recordId, assigneeId, title);
        } catch (Exception e) {
            logger.error("Error creating task from transition: {}", e.getMessage(), e);
            throw e;
        }
    }
}
