package com.cbap.security.service;

import com.cbap.persistence.entity.Role;
import com.cbap.persistence.entity.User;
import com.cbap.persistence.repository.RoleRepository;
import com.cbap.persistence.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for user management operations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordHashingService passwordHashingService;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordHashingService passwordHashingService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordHashingService = passwordHashingService;
    }

    /**
     * Create a new user (admin only).
     *
     * @param username the username
     * @param password the plain text password
     * @param email the email (optional)
     * @param roleNames the role names to assign
     * @param createdBy the user creating this user
     * @return the created user
     */
    @Transactional
    public User createUser(String username, String password, String email, List<String> roleNames, User createdBy) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        if (email != null && !email.isBlank() && userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordHashingService.hashPassword(password));
        user.setEmail(email);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setCreatedBy(createdBy);

        // Assign roles
        if (roleNames != null && !roleNames.isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : roleNames) {
                Role role = roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }

    /**
     * Get a user by ID.
     *
     * @param userId the user ID
     * @return the user
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    /**
     * Get a user by ID with roles loaded.
     *
     * @param userId the user ID
     * @return the user with roles
     */
    @Transactional(readOnly = true)
    public User getUserByIdWithRoles(UUID userId) {
        User user = getUserById(userId);
        // Force load roles
        user.getRoles().size();
        return user;
    }

    /**
     * Reset a user's password (admin only or via password reset token).
     *
     * @param userId the user ID
     * @param newPassword the new plain text password
     */
    @Transactional
    public void resetPassword(UUID userId, String newPassword) {
        User user = getUserById(userId);
        user.setPasswordHash(passwordHashingService.hashPassword(newPassword));
        // Reset failed login attempts and unlock account
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        if (user.getStatus() == User.UserStatus.LOCKED) {
            user.setStatus(User.UserStatus.ACTIVE);
        }
        userRepository.save(user);
    }

    /**
     * Get the current authenticated user.
     *
     * @param authentication the Spring Security authentication
     * @return the user
     */
    @Transactional(readOnly = true)
    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new IllegalStateException("User not authenticated");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByUsernameWithRoles(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    /**
     * Check if the current user has admin role.
     *
     * @param authentication the Spring Security authentication
     * @return true if user is admin
     */
    public boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Get all users (admin only).
     *
     * @return list of all users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
