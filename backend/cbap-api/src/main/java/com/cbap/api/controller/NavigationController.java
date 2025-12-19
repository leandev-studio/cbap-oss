package com.cbap.api.controller;

import com.cbap.api.service.NavigationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for navigation endpoints.
 */
@RestController
@RequestMapping("/api/v1/navigation")
public class NavigationController {

    private final NavigationService navigationService;

    public NavigationController(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    /**
     * Get navigation items for the current user.
     * GET /api/v1/navigation
     * 
     * Returns navigation items filtered by user's roles and permissions.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNavigation(Authentication authentication) {
        List<NavigationService.NavigationItemDTO> navigationItems = 
                navigationService.getNavigationForUser(authentication);

        Map<String, Object> response = new HashMap<>();
        response.put("items", navigationItems);
        response.put("count", navigationItems.size());

        return ResponseEntity.ok(response);
    }
}
