package com.cbap.security.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for password hashing and verification using BCrypt.
 */
@Service
public class PasswordHashingService {

    private final PasswordEncoder passwordEncoder;

    public PasswordHashingService() {
        // BCrypt with strength 12 (recommended for production)
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }

    /**
     * Hash a plain text password.
     *
     * @param plainPassword the plain text password
     * @return the hashed password
     */
    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        return passwordEncoder.encode(plainPassword);
    }

    /**
     * Verify a plain text password against a hash.
     *
     * @param plainPassword the plain text password
     * @param hashedPassword the hashed password to verify against
     * @return true if the password matches
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
}
