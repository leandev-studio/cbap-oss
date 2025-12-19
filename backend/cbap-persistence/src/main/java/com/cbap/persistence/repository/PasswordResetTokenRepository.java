package com.cbap.persistence.repository;

import com.cbap.persistence.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PasswordResetToken entity.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * Find a token by its hash.
     *
     * @param tokenHash the token hash
     * @return Optional containing the token if found
     */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * Find a valid (not expired, not used) token by hash.
     *
     * @param tokenHash the token hash
     * @return Optional containing the valid token if found
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.tokenHash = :tokenHash " +
           "AND t.expiresAt > CURRENT_TIMESTAMP AND t.usedAt IS NULL")
    Optional<PasswordResetToken> findValidTokenByHash(@Param("tokenHash") String tokenHash);

    /**
     * Find tokens for a user.
     *
     * @param userId the user ID
     * @return list of tokens for the user
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user.userId = :userId ORDER BY t.createdAt DESC")
    java.util.List<PasswordResetToken> findByUserId(@Param("userId") UUID userId);
}
