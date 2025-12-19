package com.cbap.persistence.repository;

import com.cbap.persistence.entity.NavigationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for navigation items.
 */
@Repository
public interface NavigationRepository extends JpaRepository<NavigationItem, UUID> {

    /**
     * Find all visible navigation items ordered by display order.
     */
    @Query("SELECT n FROM NavigationItem n WHERE n.visible = true ORDER BY n.displayOrder ASC, n.label ASC")
    List<NavigationItem> findAllVisibleOrdered();

    /**
     * Find navigation items by parent (for hierarchical navigation).
     */
    @Query("SELECT n FROM NavigationItem n WHERE n.parent.navigationId = :parentId AND n.visible = true ORDER BY n.displayOrder ASC, n.label ASC")
    List<NavigationItem> findByParentId(@Param("parentId") UUID parentId);

    /**
     * Find root navigation items (items with no parent).
     */
    @Query("SELECT n FROM NavigationItem n WHERE n.parent IS NULL AND n.visible = true ORDER BY n.displayOrder ASC, n.label ASC")
    List<NavigationItem> findRootItems();

    /**
     * Find navigation items by section.
     */
    @Query("SELECT n FROM NavigationItem n WHERE n.section = :section AND n.visible = true ORDER BY n.displayOrder ASC, n.label ASC")
    List<NavigationItem> findBySection(@Param("section") String section);

    /**
     * Find navigation items by required role.
     */
    @Query("SELECT n FROM NavigationItem n WHERE n.requiredRole = :roleName AND n.visible = true ORDER BY n.displayOrder ASC, n.label ASC")
    List<NavigationItem> findByRequiredRole(@Param("roleName") String roleName);

    /**
     * Find navigation items accessible by a list of roles.
     */
    @Query("SELECT n FROM NavigationItem n WHERE (n.requiredRole IS NULL OR n.requiredRole IN :roleNames) AND n.visible = true ORDER BY n.displayOrder ASC, n.label ASC")
    List<NavigationItem> findAccessibleByRoles(@Param("roleNames") List<String> roleNames);

    /**
     * Find navigation items by tenant.
     */
    @Query("SELECT n FROM NavigationItem n WHERE n.tenantId = :tenantId OR n.tenantId IS NULL ORDER BY n.displayOrder ASC, n.label ASC")
    List<NavigationItem> findByTenantId(@Param("tenantId") UUID tenantId);
}
