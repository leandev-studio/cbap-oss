package com.cbap.api.service;

import com.cbap.persistence.entity.EntityDefinition;
import com.cbap.persistence.entity.EntityRecord;
import com.cbap.persistence.entity.User;
import com.cbap.persistence.repository.EntityDefinitionRepository;
import com.cbap.persistence.repository.EntityRecordRepository;
import com.cbap.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for entity record operations.
 */
@Service
public class EntityRecordService {

    private final EntityRecordRepository entityRecordRepository;
    private final EntityDefinitionRepository entityDefinitionRepository;
    private final UserRepository userRepository;

    public EntityRecordService(
            EntityRecordRepository entityRecordRepository,
            EntityDefinitionRepository entityDefinitionRepository,
            UserRepository userRepository) {
        this.entityRecordRepository = entityRecordRepository;
        this.entityDefinitionRepository = entityDefinitionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get records for an entity with pagination.
     */
    @Transactional(readOnly = true)
    public Page<EntityRecordDTO> getRecords(String entityId, int page, int size) {
        // Verify entity exists
        EntityDefinition entity = entityDefinitionRepository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityId));

        Pageable pageable = PageRequest.of(page, size);
        Page<EntityRecord> records = entityRecordRepository.findByEntityId(entityId, pageable);

        return records.map(this::buildRecordDTO);
    }

    /**
     * Get a single record by ID.
     */
    @Transactional(readOnly = true)
    public EntityRecordDTO getRecord(String entityId, UUID recordId) {
        // Verify entity exists
        EntityDefinition entity = entityDefinitionRepository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityId));

        EntityRecord record = entityRecordRepository.findByEntityIdAndRecordId(entityId, recordId)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + recordId));

        return buildRecordDTO(record);
    }

    /**
     * Build EntityRecord DTO.
     */
    private EntityRecordDTO buildRecordDTO(EntityRecord record) {
        return new EntityRecordDTO(
                record.getRecordId().toString(),
                record.getEntity().getEntityId(),
                record.getDataJson(),
                record.getSchemaVersion(),
                record.getState(),
                record.getCreatedAt(),
                record.getUpdatedAt(),
                record.getCreatedBy() != null ? record.getCreatedBy().getUserId().toString() : null,
                record.getUpdatedBy() != null ? record.getUpdatedBy().getUserId().toString() : null
        );
    }

    // DTOs
    public static class EntityRecordDTO {
        private final String recordId;
        private final String entityId;
        private final java.util.Map<String, Object> data;
        private final Integer schemaVersion;
        private final String state;
        private final java.time.OffsetDateTime createdAt;
        private final java.time.OffsetDateTime updatedAt;
        private final String createdBy;
        private final String updatedBy;

        public EntityRecordDTO(String recordId, String entityId, java.util.Map<String, Object> data,
                              Integer schemaVersion, String state, java.time.OffsetDateTime createdAt,
                              java.time.OffsetDateTime updatedAt, String createdBy, String updatedBy) {
            this.recordId = recordId;
            this.entityId = entityId;
            this.data = data;
            this.schemaVersion = schemaVersion;
            this.state = state;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.createdBy = createdBy;
            this.updatedBy = updatedBy;
        }

        // Getters
        public String getRecordId() { return recordId; }
        public String getEntityId() { return entityId; }
        public java.util.Map<String, Object> getData() { return data; }
        public Integer getSchemaVersion() { return schemaVersion; }
        public String getState() { return state; }
        public java.time.OffsetDateTime getCreatedAt() { return createdAt; }
        public java.time.OffsetDateTime getUpdatedAt() { return updatedAt; }
        public String getCreatedBy() { return createdBy; }
        public String getUpdatedBy() { return updatedBy; }
    }
}
