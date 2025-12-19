package com.cbap.persistence.repository;

import com.cbap.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by username.
     *
     * @param username the username
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a user exists by username.
     *
     * @param username the username
     * @return true if user exists
     */
    boolean existsByUsername(String username);

    /**
     * Find a user by username, eagerly loading roles.
     *
     * @param username the username
     * @return Optional containing the user with roles loaded
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    /**
     * Find a user by email.
     *
     * @param email the email
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
}
