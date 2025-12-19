package com.cbap.security.service;

import com.cbap.persistence.entity.User;
import com.cbap.persistence.repository.UserRepository;
import com.cbap.security.config.RateLimitProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for authentication operations (login, logout, token refresh).
 */
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RateLimitProperties rateLimitProperties;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordHashingService passwordHashingService,
            JwtService jwtService,
            UserDetailsService userDetailsService,
            RateLimitProperties rateLimitProperties) {
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.rateLimitProperties = rateLimitProperties;
    }

    /**
     * Authenticate user and generate JWT tokens.
     *
     * @param username the username
     * @param password the plain text password
     * @return authentication response with access and refresh tokens
     * @throws BadCredentialsException if authentication fails
     */
    @Transactional
    public AuthenticationResponse login(String username, String password) {
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> {
                    // Don't reveal if user exists or not
                    throw new BadCredentialsException("Invalid username or password");
                });

        // Check if account is locked
        if (user.isLocked()) {
            throw new BadCredentialsException("Account is locked. Please try again later.");
        }

        // Check if account is active
        if (!user.isActive()) {
            throw new BadCredentialsException("Account is not active");
        }

        // Verify password
        if (!passwordHashingService.verifyPassword(password, user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid username or password");
        }

        // Successful login - reset failed attempts
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }

        // Load user details for token generation
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Generate tokens
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getUserId().toString());
        extraClaims.put("email", user.getEmail());
        if (user.getTenantId() != null) {
            extraClaims.put("tenantId", user.getTenantId().toString());
        }
        if (user.getFacilityId() != null) {
            extraClaims.put("facilityId", user.getFacilityId().toString());
        }

        String accessToken = jwtService.generateToken(userDetails, extraClaims);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Populate roles
        java.util.List<String> roleNames = user.getRoles().stream()
                .map(com.cbap.persistence.entity.Role::getRoleName)
                .collect(java.util.stream.Collectors.toList());

        return new AuthenticationResponse(accessToken, refreshToken, user, roleNames);
    }

    /**
     * Refresh access token using refresh token.
     *
     * @param refreshToken the refresh token
     * @return new authentication response with new tokens
     */
    @Transactional
    public AuthenticationResponse refreshToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is not active");
        }

        // Generate new tokens
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getUserId().toString());
        extraClaims.put("email", user.getEmail());
        if (user.getTenantId() != null) {
            extraClaims.put("tenantId", user.getTenantId().toString());
        }
        if (user.getFacilityId() != null) {
            extraClaims.put("facilityId", user.getFacilityId().toString());
        }

        String newAccessToken = jwtService.generateToken(userDetails, extraClaims);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        // Populate roles
        java.util.List<String> roleNames = user.getRoles().stream()
                .map(com.cbap.persistence.entity.Role::getRoleName)
                .collect(java.util.stream.Collectors.toList());

        return new AuthenticationResponse(newAccessToken, newRefreshToken, user, roleNames);
    }

    /**
     * Handle failed login attempt (rate limiting).
     */
    private void handleFailedLogin(User user) {
        int newAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(newAttempts);

        if (newAttempts >= rateLimitProperties.getLogin().getMaxAttempts()) {
            // Lock account
            user.setLockedUntil(OffsetDateTime.now().plusMinutes(
                    rateLimitProperties.getLogin().getLockoutDurationMinutes()));
            user.setStatus(User.UserStatus.LOCKED);
        }

        userRepository.save(user);
    }

    /**
     * Authentication response DTO.
     */
    public static class AuthenticationResponse {
        private final String accessToken;
        private final String refreshToken;
        private final UserInfo userInfo;

        public AuthenticationResponse(String accessToken, String refreshToken, User user, java.util.List<String> roles) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.userInfo = new UserInfo(
                    user.getUserId().toString(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getStatus().name(),
                    roles
            );
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public UserInfo getUserInfo() {
            return userInfo;
        }
    }

    /**
     * User info DTO.
     */
    public static class UserInfo {
        private final String userId;
        private final String username;
        private final String email;
        private final String status;
        private final java.util.List<String> roles;

        public UserInfo(String userId, String username, String email, String status, java.util.List<String> roles) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.status = status;
            this.roles = roles != null ? roles : new ArrayList<>();
        }

        public String getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getStatus() {
            return status;
        }

        public java.util.List<String> getRoles() {
            return roles;
        }
    }
}
