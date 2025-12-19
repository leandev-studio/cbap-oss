package com.cbap.api.controller;

import com.cbap.api.service.EntityRecordService;
import org.springframework.data.domain.Page;
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
}
