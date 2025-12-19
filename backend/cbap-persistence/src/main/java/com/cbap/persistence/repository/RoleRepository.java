package com.cbap.persistence.repository;

import com.cbap.persistence.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Role entity.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Find a role by role name.
     *
     * @param roleName the role name
     * @return Optional containing the role if found
     */
    Optional<Role> findByRoleName(String roleName);

    /**
     * Find a role by role name, eagerly loading permissions.
     *
     * @param roleName the role name
     * @return Optional containing the role with permissions loaded
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.roleName = :roleName")
    Optional<Role> findByRoleNameWithPermissions(@Param("roleName") String roleName);

    /**
     * Check if a role exists by role name.
     *
     * @param roleName the role name
     * @return true if role exists
     */
    boolean existsByRoleName(String roleName);
}
