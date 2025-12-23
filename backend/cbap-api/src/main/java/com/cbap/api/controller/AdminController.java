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

    /**
     * Get organization topology (admin only).
     * GET /api/v1/admin/org-topology
     */
    @GetMapping("/org-topology")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOrgTopology(Authentication authentication) {
        Map<String, Object> topology = adminService.getOrgTopology();
        return ResponseEntity.ok(topology);
    }

    /**
     * Create organization unit (admin only).
     * POST /api/v1/admin/org-topology
     */
    @PostMapping("/org-topology")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createOrgUnit(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Map<String, Object> orgUnit = adminService.createOrgUnit(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(orgUnit);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Update organization unit (admin only).
     * PUT /api/v1/admin/org-topology/{orgUnitId}
     */
    @PutMapping("/org-topology/{orgUnitId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateOrgUnit(
            @PathVariable String orgUnitId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Map<String, Object> orgUnit = adminService.updateOrgUnit(orgUnitId, request);
            return ResponseEntity.ok(orgUnit);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Delete organization unit (admin only).
     * DELETE /api/v1/admin/org-topology/{orgUnitId}
     */
    @DeleteMapping("/org-topology/{orgUnitId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteOrgUnit(
            @PathVariable String orgUnitId,
            Authentication authentication) {
        try {
            adminService.deleteOrgUnit(orgUnitId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Organization unit deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
