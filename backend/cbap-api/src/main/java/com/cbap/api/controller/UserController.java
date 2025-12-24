package com.cbap.api.controller;

import com.cbap.api.dto.CreateUserRequest;
import com.cbap.api.dto.PasswordResetTokenRequest;
import com.cbap.api.dto.ResetPasswordRequest;
import com.cbap.persistence.entity.Role;
import com.cbap.persistence.entity.User;
import com.cbap.security.service.PasswordResetService;
import com.cbap.security.service.UserService;
import jakarta.validation.Valid;
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
 * User management REST controller.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    public UserController(UserService userService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    /**
     * Create a new user (admin only).
     * POST /api/v1/users
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {
        try {
            User currentUser = userService.getCurrentUser(authentication);
            User createdUser = userService.createUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail(),
                    request.getRoles(),
                    currentUser
            );

            Map<String, Object> response = buildUserResponse(createdUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get all users (admin only).
     * GET /api/v1/users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUsers(Authentication authentication) {
        List<User> users = userService.getAllUsers();
        
        List<Map<String, Object>> userList = users.stream()
                .map(this::buildUserResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", userList);
        response.put("count", userList.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a user by ID.
     * GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUser(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            User currentUser = userService.getCurrentUser(authentication);
            User requestedUser = userService.getUserByIdWithRoles(id);

            // Users can only view their own profile unless they are admin
            if (!currentUser.getUserId().equals(id) && !userService.isAdmin(authentication)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Forbidden");
                error.put("message", "You can only view your own profile");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            Map<String, Object> response = buildUserResponse(requestedUser);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Update a user (admin only).
     * PUT /api/v1/users/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            User updatedUser = userService.updateUser(
                    id,
                    (String) request.get("email"),
                    (String) request.get("status"),
                    (List<String>) request.get("roles")
            );

            Map<String, Object> response = buildUserResponse(updatedUser);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Delete a user (admin only).
     * DELETE /api/v1/users/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            userService.deleteUser(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Reset a user's password (admin only).
     * PUT /api/v1/users/{id}/password
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetPassword(
            @PathVariable UUID id,
            @Valid @RequestBody ResetPasswordRequest request,
            Authentication authentication) {
        try {
            userService.resetPassword(id, request.getNewPassword());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Generate a password reset token (public endpoint, but should be rate-limited in production).
     * POST /api/v1/users/password-reset-token
     */
    @PostMapping("/password-reset-token")
    public ResponseEntity<Map<String, String>> generatePasswordResetToken(
            @Valid @RequestBody PasswordResetTokenRequest request) {
        try {
            String token = passwordResetService.generateResetToken(request.getUsernameOrEmail());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset token generated");
            response.put("token", token); // In production, send via email instead
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Reset password using a token.
     * POST /api/v1/users/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPasswordWithToken(
            @Valid @RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");

            if (token == null || newPassword == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid request");
                error.put("message", "Token and newPassword are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            passwordResetService.resetPassword(token, newPassword);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Build user response DTO.
     */
    private Map<String, Object> buildUserResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId().toString());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("status", user.getStatus().name());
        response.put("tenantId", user.getTenantId() != null ? user.getTenantId().toString() : null);
        response.put("facilityId", user.getFacilityId() != null ? user.getFacilityId().toString() : null);
        response.put("roles", user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList()));
        response.put("createdAt", user.getCreatedAt().toString());
        response.put("updatedAt", user.getUpdatedAt().toString());
        return response;
    }
}
