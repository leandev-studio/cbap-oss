package com.cbap.api.controller;

import com.cbap.api.service.ValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for validation endpoints.
 */
@RestController
@RequestMapping("/api/v1/validation")
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    /**
     * Validate a record.
     * POST /api/v1/validation/entities/{entityId}/validate
     */
    @PostMapping("/entities/{entityId}/validate")
    public ResponseEntity<Map<String, Object>> validateRecord(
            @PathVariable String entityId,
            @RequestBody ValidationRequest request,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }

        try {
            List<ValidationService.ValidationError> errors = validationService.validateRecord(
                    entityId, 
                    request.getData(), 
                    request.getTriggerEvent() != null ? request.getTriggerEvent() : "UPDATE",
                    request.getPreviousData());

            Map<String, Object> response = new HashMap<>();
            response.put("entityId", entityId);
            response.put("valid", errors.isEmpty());
            response.put("errors", errors);
            response.put("errorCount", errors.size());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad Request", "message", e.getMessage()));
        }
    }

    /**
     * Validate a specific field.
     * POST /api/v1/validation/entities/{entityId}/fields/{propertyName}/validate
     */
    @PostMapping("/entities/{entityId}/fields/{propertyName}/validate")
    public ResponseEntity<Map<String, Object>> validateField(
            @PathVariable String entityId,
            @PathVariable String propertyName,
            @RequestBody FieldValidationRequest request,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }

        try {
            List<ValidationService.ValidationError> errors = validationService.validateField(
                    entityId, 
                    propertyName, 
                    request.getValue(),
                    request.getFullRecordData() != null ? request.getFullRecordData() : Map.of());

            Map<String, Object> response = new HashMap<>();
            response.put("entityId", entityId);
            response.put("propertyName", propertyName);
            response.put("valid", errors.isEmpty());
            response.put("errors", errors);
            response.put("errorCount", errors.size());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad Request", "message", e.getMessage()));
        }
    }

    /**
     * Validation Request DTO.
     */
    public static class ValidationRequest {
        private Map<String, Object> data;
        private Map<String, Object> previousData;
        private String triggerEvent;

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }

        public Map<String, Object> getPreviousData() {
            return previousData;
        }

        public void setPreviousData(Map<String, Object> previousData) {
            this.previousData = previousData;
        }

        public String getTriggerEvent() {
            return triggerEvent;
        }

        public void setTriggerEvent(String triggerEvent) {
            this.triggerEvent = triggerEvent;
        }
    }

    /**
     * Field Validation Request DTO.
     */
    public static class FieldValidationRequest {
        private Object value;
        private Map<String, Object> fullRecordData;

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Map<String, Object> getFullRecordData() {
            return fullRecordData;
        }

        public void setFullRecordData(Map<String, Object> fullRecordData) {
            this.fullRecordData = fullRecordData;
        }
    }
}
