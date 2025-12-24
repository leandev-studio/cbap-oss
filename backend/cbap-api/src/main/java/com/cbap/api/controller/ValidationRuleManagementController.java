package com.cbap.api.controller;

import com.cbap.api.service.ValidationRuleManagementService;
import com.cbap.persistence.entity.ValidationRule;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for validation rule management (admin only).
 */
@RestController
@RequestMapping("/api/v1/admin/validation-rules")
public class ValidationRuleManagementController {

    private final ValidationRuleManagementService validationRuleManagementService;

    public ValidationRuleManagementController(ValidationRuleManagementService validationRuleManagementService) {
        this.validationRuleManagementService = validationRuleManagementService;
    }

    /**
     * Get all validation rules for an entity (admin only).
     * GET /api/v1/admin/validation-rules?entityId=Order
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getValidationRulesByEntity(
            @RequestParam(required = false) String entityId,
            Authentication authentication) {
        
        List<ValidationRule> rules;
        if (entityId != null) {
            rules = validationRuleManagementService.getValidationRulesByEntity(entityId);
        } else {
            // If no entityId, return empty list (could be extended to return all rules)
            rules = List.of();
        }
        
        List<Map<String, Object>> ruleList = rules.stream()
                .map(this::buildValidationRuleResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("validationRules", ruleList);
        response.put("count", ruleList.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a validation rule by ID (admin only).
     * GET /api/v1/admin/validation-rules/{validationId}
     */
    @GetMapping("/{validationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getValidationRule(
            @PathVariable UUID validationId,
            Authentication authentication) {
        try {
            ValidationRule rule = validationRuleManagementService.getValidationRuleById(validationId);
            Map<String, Object> response = buildValidationRuleResponse(rule);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Create a new validation rule (admin only).
     * POST /api/v1/admin/validation-rules
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createValidationRule(
            @RequestBody ValidationRuleManagementService.CreateValidationRuleRequest request,
            Authentication authentication) {
        try {
            ValidationRule rule = validationRuleManagementService.createValidationRule(request, authentication);
            
            Map<String, Object> response = buildValidationRuleResponse(rule);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Update a validation rule (admin only).
     * PUT /api/v1/admin/validation-rules/{validationId}
     */
    @PutMapping("/{validationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateValidationRule(
            @PathVariable UUID validationId,
            @RequestBody ValidationRuleManagementService.UpdateValidationRuleRequest request,
            Authentication authentication) {
        try {
            ValidationRule rule = validationRuleManagementService.updateValidationRule(validationId, request);
            
            Map<String, Object> response = buildValidationRuleResponse(rule);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Delete a validation rule (admin only).
     * DELETE /api/v1/admin/validation-rules/{validationId}
     */
    @DeleteMapping("/{validationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteValidationRule(
            @PathVariable UUID validationId,
            Authentication authentication) {
        try {
            validationRuleManagementService.deleteValidationRule(validationId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Validation rule deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // Helper method
    private Map<String, Object> buildValidationRuleResponse(ValidationRule rule) {
        Map<String, Object> response = new HashMap<>();
        response.put("validationId", rule.getValidationId().toString());
        response.put("entityId", rule.getEntity() != null ? rule.getEntity().getEntityId() : null);
        response.put("propertyName", rule.getPropertyName());
        response.put("ruleName", rule.getRuleName());
        response.put("description", rule.getDescription());
        response.put("scope", rule.getScope() != null ? rule.getScope().name() : null);
        response.put("ruleType", rule.getRuleType() != null ? rule.getRuleType().name() : null);
        response.put("expression", rule.getExpression());
        response.put("errorMessage", rule.getErrorMessage());
        response.put("errorMessageKey", rule.getErrorMessageKey());
        response.put("triggerEvents", rule.getTriggerEvents());
        response.put("conditionsJson", rule.getConditionsJson());
        response.put("metadataJson", rule.getMetadataJson());
        response.put("createdAt", rule.getCreatedAt().toString());
        response.put("updatedAt", rule.getUpdatedAt().toString());
        return response;
    }
}
