package com.cbap.persistence.repository;

import com.cbap.persistence.entity.DashboardPin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for dashboard pins.
 */
@Repository
public interface DashboardPinRepository extends JpaRepository<DashboardPin, UUID> {

    /**
     * Find all pins for a dashboard, ordered by display order.
     */
    @Query("SELECT p FROM DashboardPin p WHERE p.dashboard.dashboardId = :dashboardId ORDER BY p.displayOrder ASC, p.createdAt ASC")
    List<DashboardPin> findByDashboardId(@Param("dashboardId") UUID dashboardId);

    /**
     * Find pins by type for a dashboard.
     */
    @Query("SELECT p FROM DashboardPin p WHERE p.dashboard.dashboardId = :dashboardId AND p.pinType = :pinType ORDER BY p.displayOrder ASC")
    List<DashboardPin> findByDashboardIdAndPinType(@Param("dashboardId") UUID dashboardId, @Param("pinType") String pinType);
}
