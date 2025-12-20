package com.cbap.api.service;

import com.cbap.persistence.entity.*;
import com.cbap.persistence.repository.*;
import com.cbap.persistence.entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Service for task management.
 */
@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final com.cbap.persistence.repository.TaskRepository taskRepository;
    private final EntityDefinitionRepository entityDefinitionRepository;
    private final EntityRecordRepository entityRecordRepository;
    private final UserRepository userRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;

    public TaskService(
            TaskRepository taskRepository,
            EntityDefinitionRepository entityDefinitionRepository,
            EntityRecordRepository entityRecordRepository,
            UserRepository userRepository,
            WorkflowTransitionRepository workflowTransitionRepository) {
        this.taskRepository = taskRepository;
        this.entityDefinitionRepository = entityDefinitionRepository;
        this.entityRecordRepository = entityRecordRepository;
        this.userRepository = userRepository;
        this.workflowTransitionRepository = workflowTransitionRepository;
    }

    /**
     * Get tasks for the current user.
     */
    @Transactional(readOnly = true)
    public Page<TaskDTO> getUserTasks(UUID userId, Task.TaskStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks;
        
        if (status != null) {
            tasks = taskRepository.findByAssigneeIdAndStatus(userId, status, pageable);
        } else {
            tasks = taskRepository.findByAssigneeId(userId, pageable);
        }
        
        return tasks.map(this::toDTO);
    }

    /**
     * Get task by ID.
     */
    @Transactional(readOnly = true)
    public TaskDTO getTaskById(UUID taskId) {
        Task task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        return toDTO(task);
    }

    /**
     * Create a task.
     */
    @Transactional
    public TaskDTO createTask(CreateTaskRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        // Get entity and record
        EntityDefinition entity = entityDefinitionRepository.findById(request.getEntityId())
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + request.getEntityId()));

        EntityRecord record = entityRecordRepository.findByEntityIdAndRecordId(
                request.getEntityId(), request.getRecordId())
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + request.getRecordId()));

        // Get assignee
        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new IllegalArgumentException("Assignee not found: " + request.getAssigneeId()));

        // Get transition if provided
        WorkflowTransition transition = null;
        if (request.getTransitionId() != null) {
            transition = workflowTransitionRepository.findByTransitionId(request.getTransitionId())
                    .orElse(null);
        }

        // Create task
        Task task = new Task();
        task.setEntity(entity);
        task.setRecord(record);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssignee(assignee);
        task.setStatus(Task.TaskStatus.OPEN);
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setWorkflowState(record.getState());
        task.setTransition(transition);
        task.setCreatedBy(currentUser);

        task = taskRepository.save(task);

        logger.info("Task created: taskId={}, entityId={}, recordId={}, assigneeId={}, title={}",
                task.getTaskId(), request.getEntityId(), request.getRecordId(), request.getAssigneeId(), request.getTitle());

        return toDTO(task);
    }

    /**
     * Update a task.
     */
    @Transactional
    public TaskDTO updateTask(UUID taskId, UpdateTaskRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Task task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        // Verify user has permission (assignee or admin)
        // TODO: Add proper authorization check

        // Update fields
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        task = taskRepository.save(task);

        logger.info("Task updated: taskId={}, userId={}", taskId, currentUser.getUserId());

        return toDTO(task);
    }

    /**
     * Complete a task.
     */
    @Transactional
    public TaskDTO completeTask(UUID taskId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Task task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        // Verify user is the assignee
        if (!task.getAssignee().getUserId().equals(currentUser.getUserId())) {
            throw new IllegalStateException("Only the task assignee can complete the task");
        }

        // Update task
        task.setStatus(Task.TaskStatus.DONE);
        task.setCompletedAt(OffsetDateTime.now());
        task.setCompletedBy(currentUser);

        task = taskRepository.save(task);

        logger.info("Task completed: taskId={}, userId={}", taskId, currentUser.getUserId());

        return toDTO(task);
    }

    /**
     * Submit a decision for a task (approve/reject/request-changes).
     */
    @Transactional
    public TaskDTO submitDecision(UUID taskId, TaskDecisionRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        Task task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        // Verify user is the assignee
        if (!task.getAssignee().getUserId().equals(currentUser.getUserId())) {
            throw new IllegalStateException("Only the task assignee can submit a decision");
        }

        // Update task with decision
        task.setDecision(request.getDecision());
        task.setDecisionComments(request.getComments());
        task.setStatus(Task.TaskStatus.DONE);
        task.setCompletedAt(OffsetDateTime.now());
        task.setCompletedBy(currentUser);

        task = taskRepository.save(task);

        logger.info("Task decision submitted: taskId={}, decision={}, userId={}",
                taskId, request.getDecision(), currentUser.getUserId());

        // TODO: If assignee is in different OrgUnit than record owner, emit TASK_DECISION event
        // This will be implemented when OrgUnit support is added

        return toDTO(task);
    }

    /**
     * Convert Task entity to DTO.
     */
    private TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setTaskId(task.getTaskId().toString());
        dto.setEntityId(task.getEntity().getEntityId());
        dto.setRecordId(task.getRecord().getRecordId().toString());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setAssigneeId(task.getAssignee().getUserId().toString());
        dto.setAssigneeUsername(task.getAssignee().getUsername());
        dto.setStatus(task.getStatus().name());
        dto.setPriority(task.getPriority() != null ? task.getPriority().name() : null);
        dto.setDueDate(task.getDueDate());
        dto.setWorkflowState(task.getWorkflowState());
        dto.setTransitionId(task.getTransition() != null ? task.getTransition().getTransitionId().toString() : null);
        dto.setCompletedAt(task.getCompletedAt());
        dto.setCompletedById(task.getCompletedBy() != null ? task.getCompletedBy().getUserId().toString() : null);
        dto.setDecision(task.getDecision() != null ? task.getDecision().name() : null);
        dto.setDecisionComments(task.getDecisionComments());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setCreatedById(task.getCreatedBy() != null ? task.getCreatedBy().getUserId().toString() : null);
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
     * Task DTO.
     */
    public static class TaskDTO {
        private String taskId;
        private String entityId;
        private String recordId;
        private String title;
        private String description;
        private String assigneeId;
        private String assigneeUsername;
        private String status;
        private String priority;
        private OffsetDateTime dueDate;
        private String workflowState;
        private String transitionId;
        private OffsetDateTime completedAt;
        private String completedById;
        private String decision;
        private String decisionComments;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private String createdById;

        // Getters and Setters
        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        public String getEntityId() { return entityId; }
        public void setEntityId(String entityId) { this.entityId = entityId; }
        public String getRecordId() { return recordId; }
        public void setRecordId(String recordId) { this.recordId = recordId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getAssigneeId() { return assigneeId; }
        public void setAssigneeId(String assigneeId) { this.assigneeId = assigneeId; }
        public String getAssigneeUsername() { return assigneeUsername; }
        public void setAssigneeUsername(String assigneeUsername) { this.assigneeUsername = assigneeUsername; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public OffsetDateTime getDueDate() { return dueDate; }
        public void setDueDate(OffsetDateTime dueDate) { this.dueDate = dueDate; }
        public String getWorkflowState() { return workflowState; }
        public void setWorkflowState(String workflowState) { this.workflowState = workflowState; }
        public String getTransitionId() { return transitionId; }
        public void setTransitionId(String transitionId) { this.transitionId = transitionId; }
        public OffsetDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
        public String getCompletedById() { return completedById; }
        public void setCompletedById(String completedById) { this.completedById = completedById; }
        public String getDecision() { return decision; }
        public void setDecision(String decision) { this.decision = decision; }
        public String getDecisionComments() { return decisionComments; }
        public void setDecisionComments(String decisionComments) { this.decisionComments = decisionComments; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
        public OffsetDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
        public String getCreatedById() { return createdById; }
        public void setCreatedById(String createdById) { this.createdById = createdById; }
    }

    /**
     * Create Task Request.
     */
    public static class CreateTaskRequest {
        private String entityId;
        private UUID recordId;
        private String title;
        private String description;
        private UUID assigneeId;
        private Task.TaskPriority priority;
        private OffsetDateTime dueDate;
        private UUID transitionId;

        // Getters and Setters
        public String getEntityId() { return entityId; }
        public void setEntityId(String entityId) { this.entityId = entityId; }
        public UUID getRecordId() { return recordId; }
        public void setRecordId(UUID recordId) { this.recordId = recordId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public UUID getAssigneeId() { return assigneeId; }
        public void setAssigneeId(UUID assigneeId) { this.assigneeId = assigneeId; }
        public Task.TaskPriority getPriority() { return priority; }
        public void setPriority(Task.TaskPriority priority) { this.priority = priority; }
        public OffsetDateTime getDueDate() { return dueDate; }
        public void setDueDate(OffsetDateTime dueDate) { this.dueDate = dueDate; }
        public UUID getTransitionId() { return transitionId; }
        public void setTransitionId(UUID transitionId) { this.transitionId = transitionId; }
    }

    /**
     * Update Task Request.
     */
    public static class UpdateTaskRequest {
        private String title;
        private String description;
        private Task.TaskStatus status;
        private Task.TaskPriority priority;
        private OffsetDateTime dueDate;

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Task.TaskStatus getStatus() { return status; }
        public void setStatus(Task.TaskStatus status) { this.status = status; }
        public Task.TaskPriority getPriority() { return priority; }
        public void setPriority(Task.TaskPriority priority) { this.priority = priority; }
        public OffsetDateTime getDueDate() { return dueDate; }
        public void setDueDate(OffsetDateTime dueDate) { this.dueDate = dueDate; }
    }

    /**
     * Task Decision Request.
     */
    public static class TaskDecisionRequest {
        private Task.TaskDecision decision;
        private String comments;

        // Getters and Setters
        public Task.TaskDecision getDecision() { return decision; }
        public void setDecision(Task.TaskDecision decision) { this.decision = decision; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }
}
