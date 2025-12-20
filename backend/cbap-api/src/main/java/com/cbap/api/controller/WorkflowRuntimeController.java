package com.cbap.api.controller;

import com.cbap.api.service.WorkflowRuntimeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for workflow runtime operations.
 */
@RestController
@RequestMapping("/api/v1/entities")
public class WorkflowRuntimeController {

    private final WorkflowRuntimeService workflowRuntimeService;

    public WorkflowRuntimeController(WorkflowRuntimeService workflowRuntimeService) {
        this.workflowRuntimeService = workflowRuntimeService;
    }

    /**
     * Execute a workflow transition.
     * POST /api/v1/entities/{entityId}/records/{recordId}/transitions/{transitionId}
     */
    @PostMapping("/{entityId}/records/{recordId}/transitions/{transitionId}")
    public ResponseEntity<Map<String, Object>> executeTransition(
            @PathVariable String entityId,
            @PathVariable UUID recordId,
            @PathVariable UUID transitionId,
            @RequestBody(required = false) TransitionRequest request,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }

        if (request == null) {
            request = new TransitionRequest();
        }

        try {
            WorkflowRuntimeService.TransitionResult result = workflowRuntimeService.executeTransition(
                    entityId, recordId, transitionId, request.getComments(), authentication);

            Map<String, Object> response = new HashMap<>();
            response.put("entityId", result.getEntityId());
            response.put("recordId", result.getRecordId());
            response.put("fromState", result.getFromState());
            response.put("toState", result.getToState());
            response.put("transitionId", result.getTransitionId());
            response.put("transitionLabel", result.getTransitionLabel());
            response.put("performedBy", result.getPerformedBy());
            response.put("performedAt", result.getPerformedAt());
            response.put("comments", result.getComments());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad Request", "message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflict", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error", "message", "Failed to execute transition: " + e.getMessage()));
        }
    }

    /**
     * Get available transitions for a record.
     * GET /api/v1/entities/{entityId}/records/{recordId}/transitions
     */
    @GetMapping("/{entityId}/records/{recordId}/transitions")
    public ResponseEntity<Map<String, Object>> getAvailableTransitions(
            @PathVariable String entityId,
            @PathVariable UUID recordId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }

        try {
            List<WorkflowRuntimeService.AvailableTransitionDTO> transitions = 
                    workflowRuntimeService.getAvailableTransitions(entityId, recordId);

            Map<String, Object> response = new HashMap<>();
            response.put("entityId", entityId);
            response.put("recordId", recordId);
            response.put("transitions", transitions);
            response.put("count", transitions.size());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad Request", "message", e.getMessage()));
        }
    }

    /**
     * Get workflow audit log for a record.
     * GET /api/v1/entities/{entityId}/records/{recordId}/workflow-audit
     */
    @GetMapping("/{entityId}/records/{recordId}/workflow-audit")
    public ResponseEntity<Map<String, Object>> getWorkflowAuditLog(
            @PathVariable String entityId,
            @PathVariable UUID recordId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }

        try {
            List<WorkflowRuntimeService.WorkflowAuditLogDTO> auditLogs = 
                    workflowRuntimeService.getAuditLog(entityId, recordId);

            Map<String, Object> response = new HashMap<>();
            response.put("entityId", entityId);
            response.put("recordId", recordId);
            response.put("auditLog", auditLogs); // Frontend expects 'auditLog' not 'auditLogs'
            response.put("count", auditLogs.size());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad Request", "message", e.getMessage()));
        }
    }

    /**
     * Transition Request DTO.
     */
    public static class TransitionRequest {
        private String comments;

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }
    }
}
