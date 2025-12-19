package com.cbap.api.service;

import com.cbap.persistence.entity.NavigationItem;
import com.cbap.persistence.entity.Role;
import com.cbap.persistence.entity.User;
import com.cbap.persistence.repository.NavigationRepository;
import com.cbap.persistence.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for loading role-aware navigation items.
 */
@Service
public class NavigationService {

    private final NavigationRepository navigationRepository;
    private final UserRepository userRepository;

    public NavigationService(
            NavigationRepository navigationRepository,
            UserRepository userRepository) {
        this.navigationRepository = navigationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get navigation items accessible to the current user based on their roles.
     * 
     * @param authentication Spring Security authentication object
     * @return List of navigation items filtered by user roles and permissions
     */
    @Transactional(readOnly = true)
    public List<NavigationItemDTO> getNavigationForUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            // Return empty list for unauthenticated users
            return Collections.emptyList();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsernameWithRoles(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get user's roles
        List<String> userRoles = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        // Get user's permissions (from roles)
        Set<String> userPermissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getPermissionName())
                .collect(Collectors.toSet());

        // Load all visible navigation items
        List<NavigationItem> allItems = navigationRepository.findAllVisibleOrdered();

        // Filter items based on roles and permissions
        List<NavigationItem> accessibleItems = allItems.stream()
                .filter(item -> isItemAccessible(item, userRoles, userPermissions))
                .collect(Collectors.toList());

        // Build hierarchical structure
        return buildNavigationTree(accessibleItems);
    }

    /**
     * Check if a navigation item is accessible to the user.
     */
    private boolean isItemAccessible(NavigationItem item, List<String> userRoles, Set<String> userPermissions) {
        // If no role or permission required, item is accessible
        if (item.getRequiredRole() == null && item.getRequiredPermission() == null) {
            return true;
        }

        // Check role requirement
        if (item.getRequiredRole() != null) {
            if (!userRoles.contains(item.getRequiredRole())) {
                return false;
            }
        }

        // Check permission requirement
        if (item.getRequiredPermission() != null) {
            if (!userPermissions.contains(item.getRequiredPermission())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Build hierarchical navigation tree from flat list.
     */
    private List<NavigationItemDTO> buildNavigationTree(List<NavigationItem> items) {
        // Create a map of items by ID
        Map<UUID, NavigationItemDTO> itemMap = new HashMap<>();
        List<NavigationItemDTO> rootItems = new ArrayList<>();

        // First pass: create DTOs for all items
        for (NavigationItem item : items) {
            NavigationItemDTO dto = new NavigationItemDTO(
                    item.getNavigationId().toString(),
                    item.getLabel(),
                    item.getLabelKey(),
                    item.getIcon(),
                    item.getRoutePath(),
                    item.getDisplayOrder(),
                    item.getSection(),
                    new ArrayList<>() // children will be added in second pass
            );
            itemMap.put(item.getNavigationId(), dto);
        }

        // Second pass: build hierarchy
        for (NavigationItem item : items) {
            NavigationItemDTO dto = itemMap.get(item.getNavigationId());
            
            if (item.getParent() == null) {
                // Root item
                rootItems.add(dto);
            } else {
                // Child item - add to parent's children
                NavigationItemDTO parent = itemMap.get(item.getParent().getNavigationId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                } else {
                    // Parent not in accessible items, treat as root
                    rootItems.add(dto);
                }
            }
        }

        // Sort root items and children
        rootItems.sort(Comparator.comparing(NavigationItemDTO::getDisplayOrder)
                .thenComparing(NavigationItemDTO::getLabel));
        
        for (NavigationItemDTO dto : itemMap.values()) {
            dto.getChildren().sort(Comparator.comparing(NavigationItemDTO::getDisplayOrder)
                    .thenComparing(NavigationItemDTO::getLabel));
        }

        return rootItems;
    }

    /**
     * DTO for navigation items (for API response).
     */
    public static class NavigationItemDTO {
        private final String id;
        private final String label;
        private final String labelKey;
        private final String icon;
        private final String routePath;
        private final Integer displayOrder;
        private final String section;
        private final List<NavigationItemDTO> children;

        public NavigationItemDTO(
                String id,
                String label,
                String labelKey,
                String icon,
                String routePath,
                Integer displayOrder,
                String section,
                List<NavigationItemDTO> children) {
            this.id = id;
            this.label = label;
            this.labelKey = labelKey;
            this.icon = icon;
            this.routePath = routePath;
            this.displayOrder = displayOrder;
            this.section = section;
            this.children = children;
        }

        // Getters
        public String getId() { return id; }
        public String getLabel() { return label; }
        public String getLabelKey() { return labelKey; }
        public String getIcon() { return icon; }
        public String getRoutePath() { return routePath; }
        public Integer getDisplayOrder() { return displayOrder; }
        public String getSection() { return section; }
        public List<NavigationItemDTO> getChildren() { return children; }
    }
}
