package com.cbap.api.controller;

import com.cbap.api.service.EntityMetadataService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for entity metadata endpoints.
 */
@RestController
@RequestMapping("/api/v1/metadata/entities")
public class EntityMetadataController {

    private final EntityMetadataService entityMetadataService;

    public EntityMetadataController(EntityMetadataService entityMetadataService) {
        this.entityMetadataService = entityMetadataService;
    }

    /**
     * Get all entity definitions.
     * GET /api/v1/metadata/entities
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEntities() {
        List<EntityMetadataService.EntityDefinitionDTO> entities = entityMetadataService.getAllEntities();
        
        Map<String, Object> response = new HashMap<>();
        response.put("entities", entities);
        response.put("count", entities.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get entity definition by ID.
     * GET /api/v1/metadata/entities/{entityId}
     */
    @GetMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> getEntity(@PathVariable String entityId) {
        EntityMetadataService.EntityDefinitionDTO entity = entityMetadataService.getEntityById(entityId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("entityId", entity.getEntityId());
        response.put("name", entity.getName());
        response.put("description", entity.getDescription());
        response.put("schemaVersion", entity.getSchemaVersion());
        response.put("screenVersion", entity.getScreenVersion());
        response.put("workflowId", entity.getWorkflowId());
        response.put("authorizationModel", entity.getAuthorizationModel());
        response.put("scope", entity.getScope());
        response.put("metadataJson", entity.getMetadataJson());
        response.put("properties", entity.getProperties());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new entity definition (admin only).
     * POST /api/v1/metadata/entities
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createEntity(
            @Valid @RequestBody EntityMetadataService.CreateEntityRequest request,
            Authentication authentication) {
        
        EntityMetadataService.EntityDefinitionDTO entity = entityMetadataService.createEntity(request, authentication);
        
        Map<String, Object> response = new HashMap<>();
        response.put("entityId", entity.getEntityId());
        response.put("name", entity.getName());
        response.put("description", entity.getDescription());
        response.put("schemaVersion", entity.getSchemaVersion());
        response.put("screenVersion", entity.getScreenVersion());
        response.put("workflowId", entity.getWorkflowId());
        response.put("authorizationModel", entity.getAuthorizationModel());
        response.put("scope", entity.getScope());
        response.put("metadataJson", entity.getMetadataJson());
        response.put("properties", entity.getProperties());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update an entity definition (admin only).
     * PUT /api/v1/metadata/entities/{entityId}
     */
    @PutMapping("/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateEntity(
            @PathVariable String entityId,
            @Valid @RequestBody EntityMetadataService.UpdateEntityRequest request) {
        
        EntityMetadataService.EntityDefinitionDTO entity = entityMetadataService.updateEntity(entityId, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("entityId", entity.getEntityId());
        response.put("name", entity.getName());
        response.put("description", entity.getDescription());
        response.put("schemaVersion", entity.getSchemaVersion());
        response.put("screenVersion", entity.getScreenVersion());
        response.put("workflowId", entity.getWorkflowId());
        response.put("authorizationModel", entity.getAuthorizationModel());
        response.put("scope", entity.getScope());
        response.put("metadataJson", entity.getMetadataJson());
        response.put("properties", entity.getProperties());
        
        return ResponseEntity.ok(response);
    }
}
