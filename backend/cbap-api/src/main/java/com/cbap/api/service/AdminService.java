package com.cbap.api.service;

import com.cbap.persistence.entity.EntityDefinition;
import com.cbap.persistence.entity.EntityRecord;
import com.cbap.persistence.repository.EntityDefinitionRepository;
import com.cbap.persistence.repository.EntityRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
