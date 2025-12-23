package com.cbap.persistence.repository;

import com.cbap.persistence.entity.Measure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for measures.
 */
@Repository
public interface MeasureRepository extends JpaRepository<Measure, UUID> {

    /**
     * Find all measures by identifier (all versions).
     */
    @Query("SELECT m FROM Measure m WHERE m.measureIdentifier = :measureIdentifier ORDER BY m.version DESC")
    List<Measure> findByMeasureIdentifier(@Param("measureIdentifier") String measureIdentifier);

    /**
     * Find a specific measure by identifier and version.
     */
    @Query("SELECT m FROM Measure m WHERE m.measureIdentifier = :measureIdentifier AND m.version = :version")
    Optional<Measure> findByMeasureIdentifierAndVersion(
            @Param("measureIdentifier") String measureIdentifier,
            @Param("version") Integer version);

    /**
     * Find the latest version of a measure by identifier.
     */
    @Query("SELECT m FROM Measure m WHERE m.measureIdentifier = :measureIdentifier ORDER BY m.version DESC")
    Optional<Measure> findLatestByMeasureIdentifier(@Param("measureIdentifier") String measureIdentifier);

    /**
     * Find all measures.
     */
    @Query("SELECT m FROM Measure m ORDER BY m.measureIdentifier, m.version DESC")
    List<Measure> findAllOrdered();
}
