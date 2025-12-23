package com.cbap.persistence.repository;

import com.cbap.persistence.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Permission entity.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    /**
     * Find a permission by permission name.
     *
     * @param permissionName the permission name
     * @return Optional containing the permission if found
     */
    Optional<Permission> findByPermissionName(String permissionName);

    /**
     * Check if a permission exists by permission name.
     *
     * @param permissionName the permission name
     * @return true if permission exists
     */
    boolean existsByPermissionName(String permissionName);
}
