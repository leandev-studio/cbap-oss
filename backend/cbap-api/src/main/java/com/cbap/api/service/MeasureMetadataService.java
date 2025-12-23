package com.cbap.api.service;

import com.cbap.persistence.entity.Measure;
import com.cbap.persistence.repository.MeasureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for measure metadata operations.
 */
@Service
public class MeasureMetadataService {

    private static final Logger logger = LoggerFactory.getLogger(MeasureMetadataService.class);

    private final MeasureRepository measureRepository;

    public MeasureMetadataService(MeasureRepository measureRepository) {
        this.measureRepository = measureRepository;
    }

    /**
     * Get all measures.
     */
    @Transactional(readOnly = true)
    public List<Measure> getAllMeasures() {
        return measureRepository.findAllOrdered();
    }

    /**
     * Get all versions of a measure by identifier.
     */
    @Transactional(readOnly = true)
    public List<Measure> getMeasureVersions(String measureIdentifier) {
        return measureRepository.findByMeasureIdentifier(measureIdentifier);
    }

    /**
     * Get a specific measure by identifier and version.
     * If version is null, returns the latest version.
     */
    @Transactional(readOnly = true)
    public Optional<Measure> getMeasure(String measureIdentifier, Integer version) {
        if (version != null) {
            return measureRepository.findByMeasureIdentifierAndVersion(measureIdentifier, version);
        } else {
            return measureRepository.findLatestByMeasureIdentifier(measureIdentifier);
        }
    }

    /**
     * Get the latest version of a measure.
     */
    @Transactional(readOnly = true)
    public Optional<Measure> getLatestMeasure(String measureIdentifier) {
        return measureRepository.findLatestByMeasureIdentifier(measureIdentifier);
    }

    /**
     * Create or update a measure.
     * If a measure with the same identifier and version exists, it will be updated.
     * Otherwise, a new measure will be created.
     */
    @Transactional
    public Measure saveMeasure(Measure measure) {
        // Check if measure with same identifier and version already exists
        Optional<Measure> existing = measureRepository.findByMeasureIdentifierAndVersion(
                measure.getMeasureIdentifier(),
                measure.getVersion());

        if (existing.isPresent()) {
            Measure existingMeasure = existing.get();
            // Update existing measure
            existingMeasure.setName(measure.getName());
            existingMeasure.setDescription(measure.getDescription());
            existingMeasure.setParametersJson(measure.getParametersJson());
            existingMeasure.setReturnType(measure.getReturnType());
            existingMeasure.setDependsOnJson(measure.getDependsOnJson());
            existingMeasure.setDefinitionType(measure.getDefinitionType());
            existingMeasure.setExpression(measure.getExpression());
            existingMeasure.setMetadataJson(measure.getMetadataJson());
            return measureRepository.save(existingMeasure);
        } else {
            // Create new measure
            return measureRepository.save(measure);
        }
    }

    /**
     * Delete a measure by identifier and version.
     */
    @Transactional
    public void deleteMeasure(String measureIdentifier, Integer version) {
        Optional<Measure> measure = measureRepository.findByMeasureIdentifierAndVersion(measureIdentifier, version);
        if (measure.isPresent()) {
            measureRepository.delete(measure.get());
            logger.info("Deleted measure: {} version {}", measureIdentifier, version);
        } else {
            throw new IllegalArgumentException("Measure not found: " + measureIdentifier + " version " + version);
        }
    }
}
