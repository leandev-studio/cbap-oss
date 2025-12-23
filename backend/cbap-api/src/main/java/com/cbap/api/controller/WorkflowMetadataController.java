package com.cbap.api.controller;

import com.cbap.api.service.WorkflowMetadataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for workflow metadata endpoints.
 */
@RestController
@RequestMapping("/api/v1/metadata/workflows")
public class WorkflowMetadataController {

    private final WorkflowMetadataService workflowMetadataService;

    public WorkflowMetadataController(WorkflowMetadataService workflowMetadataService) {
        this.workflowMetadataService = workflowMetadataService;
    }

    /**
     * Get all workflow definitions.
     * GET /api/v1/metadata/workflows
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllWorkflows(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }

        List<WorkflowMetadataService.WorkflowDefinitionDTO> workflows = workflowMetadataService.getAllWorkflows();

        Map<String, Object> response = new HashMap<>();
        response.put("workflows", workflows);
        response.put("count", workflows.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get workflow definition by ID.
     * GET /api/v1/metadata/workflows/{workflowId}
     */
    @GetMapping("/{workflowId}")
    public ResponseEntity<WorkflowMetadataService.WorkflowDefinitionDTO> getWorkflow(
            @PathVariable String workflowId,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        WorkflowMetadataService.WorkflowDefinitionDTO workflow = workflowMetadataService.getWorkflowById(workflowId);
        return ResponseEntity.ok(workflow);
    }

    /**
     * Create a new workflow definition (admin only).
     * POST /api/v1/metadata/workflows
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createWorkflow(
            @RequestBody WorkflowMetadataService.CreateWorkflowRequest request,
            Authentication authentication) {
        try {
            WorkflowMetadataService.WorkflowDefinitionDTO workflow = workflowMetadataService.createWorkflow(request, authentication);
            
            Map<String, Object> response = new HashMap<>();
            response.put("workflowId", workflow.getWorkflowId());
            response.put("name", workflow.getName());
            response.put("description", workflow.getDescription());
            response.put("initialState", workflow.getInitialState());
            response.put("metadataJson", workflow.getMetadataJson());
            response.put("states", workflow.getStates());
            response.put("transitions", workflow.getTransitions());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Update a workflow definition (admin only).
     * PUT /api/v1/metadata/workflows/{workflowId}
     */
    @PutMapping("/{workflowId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateWorkflow(
            @PathVariable String workflowId,
            @RequestBody WorkflowMetadataService.UpdateWorkflowRequest request,
            Authentication authentication) {
        try {
            WorkflowMetadataService.WorkflowDefinitionDTO workflow = workflowMetadataService.updateWorkflow(workflowId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("workflowId", workflow.getWorkflowId());
            response.put("name", workflow.getName());
            response.put("description", workflow.getDescription());
            response.put("initialState", workflow.getInitialState());
            response.put("metadataJson", workflow.getMetadataJson());
            response.put("states", workflow.getStates());
            response.put("transitions", workflow.getTransitions());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Delete a workflow definition (admin only).
     * DELETE /api/v1/metadata/workflows/{workflowId}
     */
    @DeleteMapping("/{workflowId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteWorkflow(
            @PathVariable String workflowId,
            Authentication authentication) {
        try {
            workflowMetadataService.deleteWorkflow(workflowId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Workflow definition deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
