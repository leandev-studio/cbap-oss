package com.cbap.persistence.repository;

import com.cbap.persistence.entity.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for dashboards.
 */
@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, UUID> {

    /**
     * Find the default dashboard for a user.
     */
    @Query("SELECT d FROM Dashboard d WHERE d.user.userId = :userId AND d.isDefault = true")
    Optional<Dashboard> findDefaultByUserId(@Param("userId") UUID userId);

    /**
     * Find all dashboards for a user, ordered by creation date.
     */
    @Query("SELECT d FROM Dashboard d WHERE d.user.userId = :userId ORDER BY d.createdAt ASC")
    java.util.List<Dashboard> findAllByUserId(@Param("userId") UUID userId);

    /**
     * Check if user has a default dashboard.
     */
    @Query("SELECT COUNT(d) > 0 FROM Dashboard d WHERE d.user.userId = :userId AND d.isDefault = true")
    boolean existsDefaultByUserId(@Param("userId") UUID userId);
}
