package com.cbap.security.util;

import com.cbap.security.service.PasswordHashingService;

/**
 * Utility to verify a specific BCrypt hash matches a password.
 */
public class VerifyPasswordHash {

    public static void main(String[] args) {
        PasswordHashingService hashingService = new PasswordHashingService();
        
        String password = "admin123";
        // The hash from the seed data
        String hash = "$2a$12$/9LuepbRMb1MRKjiy64ZSujYI.0Ie/alpfsZWApVF2KcpFNCMSeYm";
        
        System.out.println("Verifying password hash...");
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("");
        
        boolean verified = hashingService.verifyPassword(password, hash);
        
        if (verified) {
            System.out.println("✓ SUCCESS: Hash matches password!");
        } else {
            System.out.println("✗ FAILED: Hash does NOT match password!");
            System.out.println("Generating new hash...");
            String newHash = hashingService.hashPassword(password);
            System.out.println("New hash: " + newHash);
        }
    }
}
