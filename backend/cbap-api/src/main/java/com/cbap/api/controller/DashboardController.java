package com.cbap.api.controller;

import com.cbap.api.service.DashboardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for dashboard endpoints.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Get the current user's dashboard.
     * GET /api/v1/dashboard
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard(Authentication authentication) {
        DashboardService.DashboardDTO dashboard = dashboardService.getDashboard(authentication);
        
        Map<String, Object> response = new HashMap<>();
        response.put("dashboardId", dashboard.getDashboardId());
        response.put("name", dashboard.getName());
        response.put("layoutConfig", dashboard.getLayoutConfig());
        response.put("pins", dashboard.getPins());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Add a pin to the user's dashboard.
     * POST /api/v1/dashboard/pins
     */
    @PostMapping("/pins")
    public ResponseEntity<Map<String, Object>> addPin(
            @Valid @RequestBody DashboardService.CreatePinRequest request,
            Authentication authentication) {
        
        DashboardService.DashboardPinDTO pin = dashboardService.addPin(authentication, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("pinId", pin.getPinId());
        response.put("pinType", pin.getPinType());
        response.put("title", pin.getTitle());
        response.put("description", pin.getDescription());
        response.put("config", pin.getConfig());
        response.put("displayOrder", pin.getDisplayOrder());
        response.put("widgetType", pin.getWidgetType());
        
        return ResponseEntity.ok(response);
    }
}
