package com.cbap.api.controller;

import com.cbap.api.service.EntityRecordService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for entity record endpoints.
 */
@RestController
@RequestMapping("/api/v1/entities")
public class EntityRecordController {

    private final EntityRecordService entityRecordService;

    public EntityRecordController(EntityRecordService entityRecordService) {
        this.entityRecordService = entityRecordService;
    }

    /**
     * Get records for an entity with pagination.
     * GET /api/v1/entities/{entityId}/records
     */
    @GetMapping("/{entityId}/records")
    public ResponseEntity<Map<String, Object>> getRecords(
            @PathVariable String entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        // TODO: Add authorization check - verify user has permission to read this entity
        // For now, just require authentication
        
        Page<EntityRecordService.EntityRecordDTO> records = entityRecordService.getRecords(entityId, page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", records.getContent());
        response.put("totalElements", records.getTotalElements());
        response.put("totalPages", records.getTotalPages());
        response.put("page", records.getNumber());
        response.put("size", records.getSize());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get records with database-based filtering (not using OpenSearch).
     * POST /api/v1/entities/{entityId}/records/filter
     */
    @PostMapping("/{entityId}/records/filter")
    public ResponseEntity<Map<String, Object>> getRecordsWithFilters(
            @PathVariable String entityId,
            @RequestBody(required = false) FilterRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        if (request == null) {
            request = new FilterRequest();
        }
        
        Page<EntityRecordService.EntityRecordDTO> records = entityRecordService.getRecordsWithFilters(
                entityId, 
                request.getFilters() != null ? request.getFilters() : Map.of(),
                request.getSearchText(),
                page, 
                size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", records.getContent());
        response.put("totalElements", records.getTotalElements());
        response.put("totalPages", records.getTotalPages());
        response.put("page", records.getNumber());
        response.put("size", records.getSize());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Filter request DTO.
     */
    public static class FilterRequest {
        private Map<String, Object> filters;
        private String searchText;

        public Map<String, Object> getFilters() {
            return filters;
        }

        public void setFilters(Map<String, Object> filters) {
            this.filters = filters;
        }

        public String getSearchText() {
            return searchText;
        }

        public void setSearchText(String searchText) {
            this.searchText = searchText;
        }
    }

    /**
     * Get a single record by ID.
     * GET /api/v1/entities/{entityId}/records/{recordId}
     */
    @GetMapping("/{entityId}/records/{recordId}")
    public ResponseEntity<Map<String, Object>> getRecord(
            @PathVariable String entityId,
            @PathVariable UUID recordId,
            Authentication authentication) {
        
        // TODO: Add authorization check - verify user has permission to read this entity and record
        // For now, just require authentication
        
        EntityRecordService.EntityRecordDTO record = entityRecordService.getRecord(entityId, recordId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("recordId", record.getRecordId());
        response.put("entityId", record.getEntityId());
        response.put("data", record.getData());
        response.put("schemaVersion", record.getSchemaVersion());
        response.put("state", record.getState());
        response.put("createdAt", record.getCreatedAt());
        response.put("updatedAt", record.getUpdatedAt());
        response.put("createdBy", record.getCreatedBy());
        response.put("updatedBy", record.getUpdatedBy());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new entity record.
     * POST /api/v1/entities/{entityId}/records
     */
    @PostMapping("/{entityId}/records")
    public ResponseEntity<Map<String, Object>> createRecord(
            @PathVariable String entityId,
            @Valid @RequestBody EntityRecordService.CreateRecordRequest request,
            Authentication authentication) {
        
        // Verify authentication is present
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }
        
        // TODO: Add authorization check - verify user has permission to create records for this entity
        // For now, just require authentication
        
        EntityRecordService.EntityRecordDTO record = entityRecordService.createRecord(entityId, request, authentication);
        
        Map<String, Object> response = new HashMap<>();
        response.put("recordId", record.getRecordId());
        response.put("entityId", record.getEntityId());
        response.put("data", record.getData());
        response.put("schemaVersion", record.getSchemaVersion());
        response.put("state", record.getState());
        response.put("createdAt", record.getCreatedAt());
        response.put("updatedAt", record.getUpdatedAt());
        response.put("createdBy", record.getCreatedBy());
        response.put("updatedBy", record.getUpdatedBy());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing entity record.
     * PUT /api/v1/entities/{entityId}/records/{recordId}
     */
    @PutMapping("/{entityId}/records/{recordId}")
    public ResponseEntity<Map<String, Object>> updateRecord(
            @PathVariable String entityId,
            @PathVariable UUID recordId,
            @Valid @RequestBody EntityRecordService.UpdateRecordRequest request,
            Authentication authentication) {
        
        // Verify authentication is present
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }
        
        // TODO: Add authorization check - verify user has permission to update records for this entity
        // For now, just require authentication
        
        EntityRecordService.EntityRecordDTO record = entityRecordService.updateRecord(entityId, recordId, request, authentication);
        
        Map<String, Object> response = new HashMap<>();
        response.put("recordId", record.getRecordId());
        response.put("entityId", record.getEntityId());
        response.put("data", record.getData());
        response.put("schemaVersion", record.getSchemaVersion());
        response.put("state", record.getState());
        response.put("createdAt", record.getCreatedAt());
        response.put("updatedAt", record.getUpdatedAt());
        response.put("createdBy", record.getCreatedBy());
        response.put("updatedBy", record.getUpdatedBy());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Soft delete an entity record.
     * DELETE /api/v1/entities/{entityId}/records/{recordId}
     */
    @DeleteMapping("/{entityId}/records/{recordId}")
    public ResponseEntity<Map<String, String>> deleteRecord(
            @PathVariable String entityId,
            @PathVariable UUID recordId,
            Authentication authentication) {
        
        // Verify authentication is present
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }
        
        // TODO: Add authorization check - verify user has permission to delete records for this entity
        // For now, just require authentication
        
        entityRecordService.deleteRecord(entityId, recordId, authentication);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Record deleted successfully");
        response.put("recordId", recordId.toString());
        
        return ResponseEntity.ok(response);
    }
}
