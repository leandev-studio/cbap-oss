package com.cbap.api.service;

import com.cbap.persistence.entity.Permission;
import com.cbap.persistence.entity.Role;
import com.cbap.persistence.entity.User;
import com.cbap.persistence.repository.PermissionRepository;
import com.cbap.persistence.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for role and permission management (admin only).
 */
@Service
public class RoleManagementService {

    private static final Logger logger = LoggerFactory.getLogger(RoleManagementService.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleManagementService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * Get all roles.
     */
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * Get a role by ID with permissions loaded.
     */
    @Transactional(readOnly = true)
    public Role getRoleById(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        // Force load permissions
        role.getPermissions().size();
        return role;
    }

    /**
     * Create a new role.
     */
    @Transactional
    public Role createRole(String roleName, String description, List<String> permissionNames) {
        if (roleRepository.existsByRoleName(roleName)) {
            throw new IllegalArgumentException("Role already exists: " + roleName);
        }

        Role role = new Role();
        role.setRoleName(roleName);
        role.setDescription(description);

        if (permissionNames != null && !permissionNames.isEmpty()) {
            Set<Permission> permissions = new HashSet<>();
            for (String permissionName : permissionNames) {
                Permission permission = permissionRepository.findByPermissionName(permissionName)
                        .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionName));
                permissions.add(permission);
            }
            role.setPermissions(permissions);
        }

        return roleRepository.save(role);
    }

    /**
     * Update a role.
     */
    @Transactional
    public Role updateRole(UUID roleId, String description, List<String> permissionNames) {
        Role role = getRoleById(roleId);

        if (description != null) {
            role.setDescription(description);
        }

        if (permissionNames != null) {
            Set<Permission> permissions = new HashSet<>();
            for (String permissionName : permissionNames) {
                Permission permission = permissionRepository.findByPermissionName(permissionName)
                        .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionName));
                permissions.add(permission);
            }
            role.setPermissions(permissions);
        }

        return roleRepository.save(role);
    }

    /**
     * Delete a role.
     */
    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = getRoleById(roleId);
        // Prevent deleting system roles (Admin, User, Designer, Approver)
        String roleName = role.getRoleName();
        if ("Admin".equals(roleName) || "User".equals(roleName) || 
            "Designer".equals(roleName) || "Approver".equals(roleName)) {
            throw new IllegalArgumentException("Cannot delete system role: " + roleName);
        }
        roleRepository.delete(role);
    }

    /**
     * Get all permissions.
     */
    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    /**
     * Get a permission by ID.
     */
    @Transactional(readOnly = true)
    public Permission getPermissionById(UUID permissionId) {
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));
    }

    /**
     * Assign permissions to a role.
     */
    @Transactional
    public Role assignPermissionsToRole(UUID roleId, List<String> permissionNames) {
        Role role = getRoleById(roleId);
        
        Set<Permission> permissions = new HashSet<>(role.getPermissions());
        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByPermissionName(permissionName)
                    .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionName));
            permissions.add(permission);
        }
        role.setPermissions(permissions);
        
        return roleRepository.save(role);
    }

    /**
     * Remove permissions from a role.
     */
    @Transactional
    public Role removePermissionsFromRole(UUID roleId, List<String> permissionNames) {
        Role role = getRoleById(roleId);
        
        Set<Permission> permissions = new HashSet<>(role.getPermissions());
        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByPermissionName(permissionName)
                    .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionName));
            permissions.remove(permission);
        }
        role.setPermissions(permissions);
        
        return roleRepository.save(role);
    }
}
