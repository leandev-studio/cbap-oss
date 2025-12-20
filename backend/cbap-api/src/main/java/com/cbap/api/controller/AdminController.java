package com.cbap.api.controller;

import com.cbap.api.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for admin operations.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Reindex all records for an entity.
     * POST /api/v1/admin/entities/{entityId}/reindex
     */
    @PostMapping("/entities/{entityId}/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reindexEntity(
            @PathVariable String entityId,
            Authentication authentication) {
        
        AdminService.ReindexResult result = adminService.reindexEntity(entityId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("entityId", result.getEntityId());
        response.put("totalRecords", result.getTotalRecords());
        response.put("indexedRecords", result.getIndexedRecords());
        response.put("message", "Reindexing completed successfully");
        
        return ResponseEntity.ok(response);
    }
}
