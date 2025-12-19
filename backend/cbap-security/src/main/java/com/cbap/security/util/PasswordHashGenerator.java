package com.cbap.security.util;

import com.cbap.security.service.PasswordHashingService;

/**
 * Utility class to generate BCrypt password hashes for seed data.
 * 
 * Run this as a main method to generate hashes for use in SQL migration scripts.
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        PasswordHashingService hashingService = new PasswordHashingService();
        
        String password = "admin123";
        String hash = hashingService.hashPassword(password);
        
        System.out.println("========================================");
        System.out.println("Password Hash Generator");
        System.out.println("========================================");
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("");
        
        // Verify the hash
        boolean verified = hashingService.verifyPassword(password, hash);
        System.out.println("Verification: " + (verified ? "✓ SUCCESS" : "✗ FAILED"));
        System.out.println("");
        System.out.println("Use this hash in your SQL migration:");
        System.out.println("'" + hash + "'");
    }
}
