package com.cbap.api.controller;

import com.cbap.api.service.AdminService;
import org.springframework.http.HttpStatus;
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

    /**
     * Get system settings (admin only).
     * GET /api/v1/admin/system/settings
     */
    @GetMapping("/system/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemSettings(Authentication authentication) {
        Map<String, Object> settings = adminService.getSystemSettings();
        return ResponseEntity.ok(settings);
    }

    /**
     * Update system settings (admin only).
     * PUT /api/v1/admin/system/settings
     */
    @PutMapping("/system/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateSystemSettings(
            @RequestBody Map<String, Object> settings,
            Authentication authentication) {
        Map<String, Object> updatedSettings = adminService.updateSystemSettings(settings);
        return ResponseEntity.ok(updatedSettings);
    }

    /**
     * Get licensing status (admin only).
     * GET /api/v1/admin/system/licensing
     */
    @GetMapping("/system/licensing")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLicensingStatus(Authentication authentication) {
        Map<String, Object> licensing = adminService.getLicensingStatus();
        return ResponseEntity.ok(licensing);
    }
}
