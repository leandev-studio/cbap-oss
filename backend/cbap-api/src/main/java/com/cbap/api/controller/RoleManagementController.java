package com.cbap.api.controller;

import com.cbap.api.service.RoleManagementService;
import com.cbap.persistence.entity.Permission;
import com.cbap.persistence.entity.Role;
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
 * REST controller for role and permission management (admin only).
 */
@RestController
@RequestMapping("/api/v1/admin")
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    public RoleManagementController(RoleManagementService roleManagementService) {
        this.roleManagementService = roleManagementService;
    }

    // ============================================================================
    // ROLE MANAGEMENT
    // ============================================================================

    /**
     * Get all roles (admin only).
     * GET /api/v1/admin/roles
     */
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllRoles(Authentication authentication) {
        List<Role> roles = roleManagementService.getAllRoles();
        
        List<Map<String, Object>> roleList = roles.stream()
                .map(this::buildRoleResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("roles", roleList);
        response.put("count", roleList.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a role by ID (admin only).
     * GET /api/v1/admin/roles/{roleId}
     */
    @GetMapping("/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRole(
            @PathVariable UUID roleId,
            Authentication authentication) {
        try {
            Role role = roleManagementService.getRoleById(roleId);
            Map<String, Object> response = buildRoleResponse(role);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Create a new role (admin only).
     * POST /api/v1/admin/roles
     */
    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createRole(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Role role = roleManagementService.createRole(
                    (String) request.get("roleName"),
                    (String) request.get("description"),
                    (List<String>) request.get("permissions")
            );
            
            Map<String, Object> response = buildRoleResponse(role);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Update a role (admin only).
     * PUT /api/v1/admin/roles/{roleId}
     */
    @PutMapping("/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateRole(
            @PathVariable UUID roleId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Role role = roleManagementService.updateRole(
                    roleId,
                    (String) request.get("description"),
                    (List<String>) request.get("permissions")
            );
            
            Map<String, Object> response = buildRoleResponse(role);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Delete a role (admin only).
     * DELETE /api/v1/admin/roles/{roleId}
     */
    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteRole(
            @PathVariable UUID roleId,
            Authentication authentication) {
        try {
            roleManagementService.deleteRole(roleId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Role deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ============================================================================
    // PERMISSION MANAGEMENT
    // ============================================================================

    /**
     * Get all permissions (admin only).
     * GET /api/v1/admin/permissions
     */
    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllPermissions(Authentication authentication) {
        List<Permission> permissions = roleManagementService.getAllPermissions();
        
        List<Map<String, Object>> permissionList = permissions.stream()
                .map(this::buildPermissionResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("permissions", permissionList);
        response.put("count", permissionList.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a permission by ID (admin only).
     * GET /api/v1/admin/permissions/{permissionId}
     */
    @GetMapping("/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPermission(
            @PathVariable UUID permissionId,
            Authentication authentication) {
        try {
            Permission permission = roleManagementService.getPermissionById(permissionId);
            Map<String, Object> response = buildPermissionResponse(permission);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Assign permissions to a role (admin only).
     * POST /api/v1/admin/roles/{roleId}/permissions
     */
    @PostMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> assignPermissionsToRole(
            @PathVariable UUID roleId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Role role = roleManagementService.assignPermissionsToRole(
                    roleId,
                    (List<String>) request.get("permissions")
            );
            
            Map<String, Object> response = buildRoleResponse(role);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Remove permissions from a role (admin only).
     * DELETE /api/v1/admin/roles/{roleId}/permissions
     */
    @DeleteMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> removePermissionsFromRole(
            @PathVariable UUID roleId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Role role = roleManagementService.removePermissionsFromRole(
                    roleId,
                    (List<String>) request.get("permissions")
            );
            
            Map<String, Object> response = buildRoleResponse(role);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    private Map<String, Object> buildRoleResponse(Role role) {
        Map<String, Object> response = new HashMap<>();
        response.put("roleId", role.getRoleId().toString());
        response.put("roleName", role.getRoleName());
        response.put("description", role.getDescription());
        response.put("tenantId", role.getTenantId() != null ? role.getTenantId().toString() : null);
        response.put("permissions", role.getPermissions().stream()
                .map(this::buildPermissionResponse)
                .collect(Collectors.toList()));
        response.put("createdAt", role.getCreatedAt().toString());
        response.put("updatedAt", role.getUpdatedAt().toString());
        return response;
    }

    private Map<String, Object> buildPermissionResponse(Permission permission) {
        Map<String, Object> response = new HashMap<>();
        response.put("permissionId", permission.getPermissionId().toString());
        response.put("permissionName", permission.getPermissionName());
        response.put("resourceType", permission.getResourceType());
        response.put("action", permission.getAction());
        response.put("description", permission.getDescription());
        response.put("createdAt", permission.getCreatedAt().toString());
        return response;
    }
}
