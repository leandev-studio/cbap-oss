package com.cbap.api.controller;

import com.cbap.api.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for task endpoints.
 */
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;
    private final com.cbap.persistence.repository.UserRepository userRepository;

    public TaskController(
            TaskService taskService,
            com.cbap.persistence.repository.UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    /**
     * Get current user's tasks.
     * GET /api/v1/tasks?status={status}&page={page}&size={size}
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }

        // Get current user
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        UUID userId = userRepository.findByUsername(username)
                .map(com.cbap.persistence.entity.User::getUserId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        // Parse status if provided
        com.cbap.persistence.entity.Task.TaskStatus taskStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                taskStatus = com.cbap.persistence.entity.Task.TaskStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Bad Request", "message", "Invalid status: " + status));
            }
        }

        Page<TaskService.TaskDTO> tasks = taskService.getUserTasks(userId, taskStatus, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("tasks", tasks.getContent());
        response.put("totalElements", tasks.getTotalElements());
        response.put("totalPages", tasks.getTotalPages());
        response.put("page", tasks.getNumber());
        response.put("size", tasks.getSize());

        return ResponseEntity.ok(response);
    }

    /**
     * Get task by ID.
     * GET /api/v1/tasks/{taskId}
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskService.TaskDTO> getTask(
            @PathVariable UUID taskId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TaskService.TaskDTO task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }

    /**
     * Complete a task.
     * POST /api/v1/tasks/{taskId}/complete
     */
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Map<String, Object>> completeTask(
            @PathVariable UUID taskId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }

        try {
            TaskService.TaskDTO task = taskService.completeTask(taskId, authentication);

            Map<String, Object> response = new HashMap<>();
            response.put("taskId", task.getTaskId());
            response.put("status", task.getStatus());
            response.put("completedAt", task.getCompletedAt());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Forbidden", "message", e.getMessage()));
        }
    }

    /**
     * Submit a decision for a task (approve/reject/request-changes).
     * POST /api/v1/tasks/{taskId}/decisions
     */
    @PostMapping("/{taskId}/decisions")
    public ResponseEntity<Map<String, Object>> submitDecision(
            @PathVariable UUID taskId,
            @RequestBody TaskDecisionRequest request,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }

        if (request.getDecision() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad Request", "message", "Decision is required"));
        }

        try {
            TaskService.TaskDecisionRequest decisionRequest = new TaskService.TaskDecisionRequest();
            decisionRequest.setDecision(request.getDecision());
            decisionRequest.setComments(request.getComments());

            TaskService.TaskDTO task = taskService.submitDecision(taskId, decisionRequest, authentication);

            Map<String, Object> response = new HashMap<>();
            response.put("taskId", task.getTaskId());
            response.put("status", task.getStatus());
            response.put("decision", task.getDecision());
            response.put("decisionComments", task.getDecisionComments());
            response.put("completedAt", task.getCompletedAt());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Forbidden", "message", e.getMessage()));
        }
    }

    /**
     * Task Decision Request DTO.
     */
    public static class TaskDecisionRequest {
        private com.cbap.persistence.entity.Task.TaskDecision decision;
        private String comments;

        public com.cbap.persistence.entity.Task.TaskDecision getDecision() {
            return decision;
        }

        public void setDecision(com.cbap.persistence.entity.Task.TaskDecision decision) {
            this.decision = decision;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }
    }
}
