package com.cbap.api.controller;

import com.cbap.api.dto.LoginRequest;
import com.cbap.api.dto.RefreshTokenRequest;
import com.cbap.persistence.entity.Role;
import com.cbap.persistence.entity.User;
import com.cbap.persistence.repository.UserRepository;
import com.cbap.security.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Authentication REST controller.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationService authenticationService, UserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
    }

    /**
     * Login endpoint.
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthenticationService.AuthenticationResponse response = authenticationService.login(
                    request.getUsername(),
                    request.getPassword()
            );

            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", response.getAccessToken());
            result.put("refreshToken", response.getRefreshToken());
            result.put("user", buildUserResponse(response.getUserInfo()));

            return ResponseEntity.ok(result);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid credentials");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * Refresh token endpoint.
     * POST /api/v1/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthenticationService.AuthenticationResponse response = authenticationService.refreshToken(
                    request.getRefreshToken()
            );

            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", response.getAccessToken());
            result.put("refreshToken", response.getRefreshToken());
            result.put("user", buildUserResponse(response.getUserInfo()));

            return ResponseEntity.ok(result);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid refresh token");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * Get current user endpoint.
     * GET /api/v1/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsernameWithRoles(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("userId", user.getUserId().toString());
        userResponse.put("username", user.getUsername());
        userResponse.put("email", user.getEmail());
        userResponse.put("status", user.getStatus().name());
        userResponse.put("roles", user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(userResponse);
    }

    /**
     * Logout endpoint (client-side token removal, server-side is stateless).
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // JWT is stateless, so logout is handled client-side by removing the token
        // In a production system, you might want to maintain a token blacklist
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Build user response from UserInfo.
     */
    private Map<String, Object> buildUserResponse(AuthenticationService.UserInfo userInfo) {
        Map<String, Object> user = new HashMap<>();
        user.put("userId", userInfo.getUserId());
        user.put("username", userInfo.getUsername());
        user.put("email", userInfo.getEmail());
        user.put("status", userInfo.getStatus());
        user.put("roles", userInfo.getRoles());
        return user;
    }
}
