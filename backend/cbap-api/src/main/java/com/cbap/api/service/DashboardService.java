package com.cbap.api.service;

import com.cbap.persistence.entity.Dashboard;
import com.cbap.persistence.entity.DashboardPin;
import com.cbap.persistence.entity.User;
import com.cbap.persistence.repository.DashboardPinRepository;
import com.cbap.persistence.repository.DashboardRepository;
import com.cbap.persistence.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for dashboard operations.
 */
@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final DashboardPinRepository dashboardPinRepository;
    private final UserRepository userRepository;

    public DashboardService(
            DashboardRepository dashboardRepository,
            DashboardPinRepository dashboardPinRepository,
            UserRepository userRepository) {
        this.dashboardRepository = dashboardRepository;
        this.dashboardPinRepository = dashboardPinRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get or create the default dashboard for the current user.
     */
    @Transactional
    public DashboardDTO getDashboard(Authentication authentication) {
        User user = getCurrentUser(authentication);
        
        // Try to find default dashboard
        Optional<Dashboard> dashboardOpt = dashboardRepository.findDefaultByUserId(user.getUserId());
        
        Dashboard dashboard;
        if (dashboardOpt.isPresent()) {
            dashboard = dashboardOpt.get();
        } else {
            // Create default dashboard
            dashboard = new Dashboard();
            dashboard.setUser(user);
            dashboard.setName("My Dashboard");
            dashboard.setIsDefault(true);
            dashboard = dashboardRepository.save(dashboard);
        }

        // Load pins
        List<DashboardPin> pins = dashboardPinRepository.findByDashboardId(dashboard.getDashboardId());
        
        return buildDashboardDTO(dashboard, pins);
    }

    /**
     * Add a pin to the user's default dashboard.
     */
    @Transactional
    public DashboardPinDTO addPin(Authentication authentication, CreatePinRequest request) {
        User user = getCurrentUser(authentication);
        
        // Get or create default dashboard
        Dashboard dashboard = dashboardRepository.findDefaultByUserId(user.getUserId())
                .orElseGet(() -> {
                    Dashboard newDashboard = new Dashboard();
                    newDashboard.setUser(user);
                    newDashboard.setName("My Dashboard");
                    newDashboard.setIsDefault(true);
                    return dashboardRepository.save(newDashboard);
                });

        // Create pin
        DashboardPin pin = new DashboardPin();
        pin.setDashboard(dashboard);
        pin.setPinType(request.getPinType().toUpperCase());
        pin.setTitle(request.getTitle());
        pin.setDescription(request.getDescription());
        pin.setConfigJson(request.getConfig());
        pin.setWidgetType(request.getWidgetType());
        
        // Set display order (append to end)
        List<DashboardPin> existingPins = dashboardPinRepository.findByDashboardId(dashboard.getDashboardId());
        int maxOrder = existingPins.stream()
                .mapToInt(DashboardPin::getDisplayOrder)
                .max()
                .orElse(-1);
        pin.setDisplayOrder(maxOrder + 1);
        
        pin = dashboardPinRepository.save(pin);
        
        return buildPinDTO(pin);
    }

    /**
     * Get current user from authentication.
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new RuntimeException("User not authenticated");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Build Dashboard DTO.
     */
    private DashboardDTO buildDashboardDTO(Dashboard dashboard, List<DashboardPin> pins) {
        List<DashboardPinDTO> pinDTOs = pins.stream()
                .map(this::buildPinDTO)
                .collect(Collectors.toList());
        
        return new DashboardDTO(
                dashboard.getDashboardId().toString(),
                dashboard.getName(),
                dashboard.getLayoutConfig(),
                pinDTOs
        );
    }

    /**
     * Build DashboardPin DTO.
     */
    private DashboardPinDTO buildPinDTO(DashboardPin pin) {
        return new DashboardPinDTO(
                pin.getPinId().toString(),
                pin.getPinType(),
                pin.getTitle(),
                pin.getDescription(),
                pin.getConfigJson(),
                pin.getDisplayOrder(),
                pin.getWidgetType()
        );
    }

    /**
     * DTOs
     */
    public static class DashboardDTO {
        private final String dashboardId;
        private final String name;
        private final Map<String, Object> layoutConfig;
        private final List<DashboardPinDTO> pins;

        public DashboardDTO(String dashboardId, String name, Map<String, Object> layoutConfig, List<DashboardPinDTO> pins) {
            this.dashboardId = dashboardId;
            this.name = name;
            this.layoutConfig = layoutConfig;
            this.pins = pins;
        }

        public String getDashboardId() { return dashboardId; }
        public String getName() { return name; }
        public Map<String, Object> getLayoutConfig() { return layoutConfig; }
        public List<DashboardPinDTO> getPins() { return pins; }
    }

    public static class DashboardPinDTO {
        private final String pinId;
        private final String pinType;
        private final String title;
        private final String description;
        private final Map<String, Object> config;
        private final Integer displayOrder;
        private final String widgetType;

        public DashboardPinDTO(String pinId, String pinType, String title, String description,
                               Map<String, Object> config, Integer displayOrder, String widgetType) {
            this.pinId = pinId;
            this.pinType = pinType;
            this.title = title;
            this.description = description;
            this.config = config;
            this.displayOrder = displayOrder;
            this.widgetType = widgetType;
        }

        public String getPinId() { return pinId; }
        public String getPinType() { return pinType; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public Map<String, Object> getConfig() { return config; }
        public Integer getDisplayOrder() { return displayOrder; }
        public String getWidgetType() { return widgetType; }
    }

    public static class CreatePinRequest {
        private String pinType;
        private String title;
        private String description;
        private Map<String, Object> config;
        private String widgetType;

        public String getPinType() { return pinType; }
        public void setPinType(String pinType) { this.pinType = pinType; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Map<String, Object> getConfig() { return config; }
        public void setConfig(Map<String, Object> config) { this.config = config; }
        public String getWidgetType() { return widgetType; }
        public void setWidgetType(String widgetType) { this.widgetType = widgetType; }
    }
}
