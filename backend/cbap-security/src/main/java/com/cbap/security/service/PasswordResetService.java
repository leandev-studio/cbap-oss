package com.cbap.security.service;

import com.cbap.persistence.entity.PasswordResetToken;
import com.cbap.persistence.entity.User;
import com.cbap.persistence.repository.PasswordResetTokenRepository;
import com.cbap.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Service for password reset token generation and validation.
 */
@Service
public class PasswordResetService {

    private static final int TOKEN_LENGTH = 32;
    private static final int TOKEN_EXPIRATION_HOURS = 24;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordHashingService passwordHashingService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
    }

    /**
     * Generate a password reset token for a user.
     *
     * @param userId the user ID
     * @return the plain text token (should be sent to user, not stored)
     */
    @Transactional
    public String generateResetToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Invalidate any existing valid tokens for this user
        invalidateUserTokens(userId);

        // Generate a secure random token
        String plainToken = generateSecureToken();
        // Use SHA-256 for token hashing (deterministic, allows searching)
        String tokenHash = hashToken(plainToken);

        // Create and save token
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(tokenHash);
        token.setExpiresAt(OffsetDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS));

        tokenRepository.save(token);

        return plainToken;
    }

    /**
     * Generate a password reset token by username or email.
     *
     * @param usernameOrEmail the username or email
     * @return the plain text token
     */
    @Transactional
    public String generateResetToken(String usernameOrEmail) {
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new IllegalArgumentException("User not found: " + usernameOrEmail)));

        return generateResetToken(user.getUserId());
    }

    /**
     * Validate and use a password reset token.
     *
     * @param plainToken the plain text token
     * @return the user ID if token is valid
     * @throws IllegalArgumentException if token is invalid or expired
     */
    @Transactional
    public UUID validateAndUseToken(String plainToken) {
        // Hash the token to search for it (SHA-256 is deterministic)
        String tokenHash = hashToken(plainToken);
        
        PasswordResetToken token = tokenRepository.findValidTokenByHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        // Mark token as used
        token.setUsedAt(OffsetDateTime.now());
        tokenRepository.save(token);

        return token.getUser().getUserId();
    }

    /**
     * Reset password using a token.
     *
     * @param plainToken the plain text token
     * @param newPassword the new password
     * @return the user ID
     */
    @Transactional
    public UUID resetPassword(String plainToken, String newPassword) {
        UUID userId = validateAndUseToken(plainToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update password
        user.setPasswordHash(passwordHashingService.hashPassword(newPassword));
        // Reset failed login attempts and unlock account
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        if (user.getStatus() == User.UserStatus.LOCKED) {
            user.setStatus(User.UserStatus.ACTIVE);
        }

        userRepository.save(user);

        return userId;
    }

    /**
     * Invalidate all valid tokens for a user.
     */
    @Transactional
    public void invalidateUserTokens(UUID userId) {
        java.util.List<PasswordResetToken> tokens = tokenRepository.findByUserId(userId);
        OffsetDateTime now = OffsetDateTime.now();

        for (PasswordResetToken token : tokens) {
            if (token.isValid()) {
                token.setUsedAt(now);
                tokenRepository.save(token);
            }
        }
    }

    /**
     * Generate a secure random token.
     */
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[TOKEN_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Hash a token using SHA-256 (deterministic, allows searching).
     * Note: This is different from password hashing (BCrypt) because we need
     * to be able to search for tokens by hash.
     *
     * @param token the plain text token
     * @return the SHA-256 hash
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
