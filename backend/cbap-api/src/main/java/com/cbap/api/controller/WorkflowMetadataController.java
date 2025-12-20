package com.cbap.api.controller;

import com.cbap.api.service.WorkflowMetadataService;
import org.springframework.http.ResponseEntity;
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
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
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
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        WorkflowMetadataService.WorkflowDefinitionDTO workflow = workflowMetadataService.getWorkflowById(workflowId);
        return ResponseEntity.ok(workflow);
    }
}
