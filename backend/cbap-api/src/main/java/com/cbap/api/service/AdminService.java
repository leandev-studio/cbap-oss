package com.cbap.api.service;

import com.cbap.persistence.entity.EntityDefinition;
import com.cbap.persistence.entity.EntityRecord;
import com.cbap.persistence.repository.EntityDefinitionRepository;
import com.cbap.persistence.repository.EntityRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for admin operations.
 */
@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final EntityDefinitionRepository entityDefinitionRepository;
    private final EntityRecordRepository entityRecordRepository;
    private final com.cbap.search.service.SearchIndexingService searchIndexingService;

    public AdminService(
            EntityDefinitionRepository entityDefinitionRepository,
            EntityRecordRepository entityRecordRepository,
            com.cbap.search.service.SearchIndexingService searchIndexingService) {
        this.entityDefinitionRepository = entityDefinitionRepository;
        this.entityRecordRepository = entityRecordRepository;
        this.searchIndexingService = searchIndexingService;
    }

    /**
     * Reindex all records for an entity.
     */
    @Transactional(readOnly = true)
    public ReindexResult reindexEntity(String entityId) {
        // Get entity definition with properties
        EntityDefinition entity = entityDefinitionRepository.findByEntityIdWithProperties(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityId));

        // Get all records for this entity (including deleted)
        List<EntityRecord> records = entityRecordRepository.findAllByEntityId(entityId);

        logger.info("Starting reindex for entity: entityId={}, recordCount={}", entityId, records.size());

        // Reindex all records
        int indexedCount = searchIndexingService.reindexAllRecords(entity, records);

        return new ReindexResult(entityId, records.size(), indexedCount);
    }

    /**
     * Get system settings.
     * Note: In a production system, this would read from a system_settings table.
     * For now, return default/placeholder values.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("applicationName", "CBAP OSS");
        settings.put("version", "1.0.0");
        settings.put("maxFileUploadSize", "10MB");
        settings.put("sessionTimeout", 3600);
        settings.put("enableAuditLogging", true);
        // Add more settings as needed
        return settings;
    }

    /**
     * Update system settings.
     * Note: In a production system, this would update a system_settings table.
     * For now, just return the provided settings (no persistence).
     */
    @Transactional
    public Map<String, Object> updateSystemSettings(Map<String, Object> settings) {
        // In production, persist to system_settings table
        // For now, just return the settings
        logger.info("System settings updated: {}", settings);
        return settings;
    }

    /**
     * Get licensing status.
     * Note: In a production system, this would check license validity, expiration, etc.
     * For OSS, return a basic status indicating it's an open-source version.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getLicensingStatus() {
        Map<String, Object> licensing = new HashMap<>();
        licensing.put("licenseType", "OSS");
        licensing.put("status", "ACTIVE");
        licensing.put("expirationDate", null);
        licensing.put("maxUsers", null);
        licensing.put("currentUsers", 0);
        licensing.put("features", List.of("all")); // OSS has all features
        return licensing;
    }

    /**
     * Result of reindex operation.
     */
    public static class ReindexResult {
        private final String entityId;
        private final int totalRecords;
        private final int indexedRecords;

        public ReindexResult(String entityId, int totalRecords, int indexedRecords) {
            this.entityId = entityId;
            this.totalRecords = totalRecords;
            this.indexedRecords = indexedRecords;
        }

        public String getEntityId() { return entityId; }
        public int getTotalRecords() { return totalRecords; }
        public int getIndexedRecords() { return indexedRecords; }
    }
}
