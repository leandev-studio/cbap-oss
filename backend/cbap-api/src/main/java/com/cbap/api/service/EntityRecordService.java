package com.cbap.api.service;

import com.cbap.persistence.entity.EntityDefinition;
import com.cbap.persistence.entity.EntityRecord;
import com.cbap.persistence.entity.PropertyDefinition;
import com.cbap.persistence.entity.User;
import com.cbap.persistence.repository.EntityDefinitionRepository;
import com.cbap.persistence.repository.EntityRecordRepository;
import com.cbap.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Service for entity record operations.
 */
@Service
public class EntityRecordService {

    private static final Logger logger = LoggerFactory.getLogger(EntityRecordService.class);

    private final EntityRecordRepository entityRecordRepository;
    private final EntityDefinitionRepository entityDefinitionRepository;
    private final UserRepository userRepository;
    private final com.cbap.search.service.SearchIndexingService searchIndexingService;
    private final ValidationService validationService;

    public EntityRecordService(
            EntityRecordRepository entityRecordRepository,
            EntityDefinitionRepository entityDefinitionRepository,
            UserRepository userRepository,
            com.cbap.search.service.SearchIndexingService searchIndexingService,
            ValidationService validationService) {
        this.entityRecordRepository = entityRecordRepository;
        this.entityDefinitionRepository = entityDefinitionRepository;
        this.userRepository = userRepository;
        this.searchIndexingService = searchIndexingService;
        this.validationService = validationService;
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
     * Get records with database-based filtering and search.
     * This uses PostgreSQL JSONB queries, not OpenSearch.
     * Filters are applied directly to the JSONB data_json field.
     */
    @Transactional(readOnly = true)
    public Page<EntityRecordDTO> getRecordsWithFilters(
            String entityId, 
            Map<String, Object> filters, 
            String searchText,
            int page, 
            int size) {
        // Verify entity exists
        EntityDefinition entity = entityDefinitionRepository.findByEntityIdWithProperties(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityId));

        // Get all records and filter in memory (simple approach for now)
        // TODO: Optimize with native SQL queries for better performance
        Pageable pageable = PageRequest.of(page, size);
        Page<EntityRecord> allRecords = entityRecordRepository.findByEntityId(entityId, Pageable.unpaged());
        
        // Filter records based on filters and searchText
        List<EntityRecord> filteredRecords = allRecords.getContent().stream()
                .filter(record -> {
                    if (record.getDataJson() == null) {
                        return false;
                    }
                    
                    // Apply text search across all fields, including reference display values
                    if (searchText != null && !searchText.trim().isEmpty()) {
                        String searchLower = searchText.toLowerCase();
                        boolean matches = false;
                        
                        // First, check direct field values
                        String dataJsonText = record.getDataJson().toString().toLowerCase();
                        if (dataJsonText.contains(searchLower)) {
                            matches = true;
                        } else {
                            // Check reference fields by resolving their display values
                            if (entity.getProperties() != null) {
                                for (PropertyDefinition property : entity.getProperties()) {
                                    if ("reference".equals(property.getPropertyType()) 
                                            && property.getReferenceEntity() != null) {
                                        Object refValue = record.getDataJson().get(property.getPropertyName());
                                        if (refValue != null) {
                                            try {
                                                String referenceId = refValue.toString();
                                                UUID referenceUuid = UUID.fromString(referenceId);
                                                
                                                // Fetch the referenced record
                                                EntityRecord referencedRecord = entityRecordRepository
                                                        .findByEntityIdAndRecordId(
                                                                property.getReferenceEntity().getEntityId(), 
                                                                referenceUuid)
                                                        .orElse(null);
                                                
                                                if (referencedRecord != null && referencedRecord.getDataJson() != null) {
                                                    // Get displayField from metadata
                                                    String displayField = null;
                                                    if (property.getMetadataJson() != null) {
                                                        Object displayFieldObj = property.getMetadataJson().get("displayField");
                                                        if (displayFieldObj instanceof String) {
                                                            displayField = (String) displayFieldObj;
                                                        }
                                                    }
                                                    
                                                    // Try to get display value
                                                    String displayValue = null;
                                                    if (displayField != null) {
                                                        Object fieldValue = referencedRecord.getDataJson().get(displayField);
                                                        if (fieldValue != null) {
                                                            displayValue = fieldValue.toString().toLowerCase();
                                                        }
                                                    }
                                                    
                                                    // Fallback to common field names
                                                    if (displayValue == null) {
                                                        String[] commonFields = {"name", "companyName", "title", "label"};
                                                        for (String field : commonFields) {
                                                            Object fieldValue = referencedRecord.getDataJson().get(field);
                                                            if (fieldValue != null) {
                                                                displayValue = fieldValue.toString().toLowerCase();
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    
                                                    // Check if search text matches display value
                                                    if (displayValue != null && displayValue.contains(searchLower)) {
                                                        matches = true;
                                                        break;
                                                    }
                                                }
                                            } catch (Exception e) {
                                                // Invalid UUID or other error, skip this reference
                                                logger.debug("Error resolving reference for search: property={}, error={}", 
                                                        property.getPropertyName(), e.getMessage());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (!matches) {
                            return false;
                        }
                    }
                    
                    // Apply property filters
                    for (Map.Entry<String, Object> filterEntry : filters.entrySet()) {
                        String propertyName = filterEntry.getKey();
                        Object filterValue = filterEntry.getValue();
                        
                        if (filterValue == null || filterValue.toString().trim().isEmpty()) {
                            continue;
                        }
                        
                        Object recordValue = record.getDataJson().get(propertyName);
                        if (recordValue == null) {
                            return false;
                        }
                        
                        // Handle different filter value types
                        if (filterValue instanceof Map) {
                            // Date range filter: {"from": "2024-01-01", "to": "2024-12-31"}
                            @SuppressWarnings("unchecked")
                            Map<String, Object> rangeMap = (Map<String, Object>) filterValue;
                            // For now, skip complex range filters - can be enhanced later
                            continue;
                        } else if (filterValue instanceof List) {
                            // Multi-value filter
                            @SuppressWarnings("unchecked")
                            List<Object> filterValues = (List<Object>) filterValue;
                            if (!filterValues.contains(recordValue.toString())) {
                                return false;
                            }
                        } else {
                            // Exact match or contains match
                            String filterStr = filterValue.toString().toLowerCase();
                            String recordStr = recordValue.toString().toLowerCase();
                            if (!recordStr.contains(filterStr)) {
                                return false;
                            }
                        }
                    }
                    
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredRecords.size());
        List<EntityRecord> pagedRecords = start < filteredRecords.size() 
                ? filteredRecords.subList(start, end) 
                : List.of();
        
        Page<EntityRecord> result = new org.springframework.data.domain.PageImpl<>(
                pagedRecords, pageable, filteredRecords.size());
        
        return result.map(this::buildRecordDTO);
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
     * Create a new entity record.
     */
    @Transactional
    public EntityRecordDTO createRecord(String entityId, CreateRecordRequest request, Authentication authentication) {
        // Get entity definition with properties
        EntityDefinition entity = entityDefinitionRepository.findByEntityIdWithProperties(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityId));

        // Get current user
        User user = getCurrentUser(authentication);

        // TODO: Authorization check - verify user has ENTITY_CREATE permission for this entity
        // For now, just require authentication

        // Validate data against entity definition
        validateRecordData(entity, request.getData(), true);
        
        // Validate using validation rules
        List<ValidationService.ValidationError> validationErrors = validationService.validateRecord(
                entityId, request.getData(), "CREATE", null);
        if (!validationErrors.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation failed: ");
            for (ValidationService.ValidationError error : validationErrors) {
                if (error.getPropertyName() != null) {
                    errorMessage.append(error.getPropertyName()).append(": ");
                }
                errorMessage.append(error.getMessage()).append("; ");
            }
            throw new IllegalArgumentException(errorMessage.toString());
        }

        // Create record
        EntityRecord record = new EntityRecord();
        record.setEntity(entity);
        record.setDataJson(request.getData());
        record.setSchemaVersion(entity.getSchemaVersion());
        // Set initial state if workflow is assigned, otherwise use provided state
        if (entity.getWorkflowId() != null && !entity.getWorkflowId().isEmpty()) {
            // State will be set to initial state on first workflow transition
            record.setState(null);
        } else {
            record.setState(request.getState());
        }
        record.setCreatedBy(user);
        record.setUpdatedBy(user);

        record = entityRecordRepository.save(record);

        // Index in OpenSearch (async, non-blocking)
        try {
            searchIndexingService.indexRecord(entity, record);
        } catch (Exception e) {
            logger.warn("Failed to index record in search: entityId={}, recordId={}", 
                    entityId, record.getRecordId(), e);
            // Don't fail the operation if indexing fails
        }

        // Audit log
        logger.info("Entity record created: entityId={}, recordId={}, userId={}", 
                entityId, record.getRecordId(), user.getUserId());

        return buildRecordDTO(record);
    }

    /**
     * Update an existing entity record.
     */
    @Transactional
    public EntityRecordDTO updateRecord(String entityId, UUID recordId, UpdateRecordRequest request, Authentication authentication) {
        // Get entity definition with properties
        EntityDefinition entity = entityDefinitionRepository.findByEntityIdWithProperties(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityId));

        // Get record
        EntityRecord existingRecord = entityRecordRepository.findByEntityIdAndRecordId(entityId, recordId)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + recordId));

        // Get current user
        User user = getCurrentUser(authentication);

        // TODO: Authorization check - verify user has ENTITY_UPDATE permission for this entity
        // For now, just require authentication

        // Get previous record data for validation context
        Map<String, Object> previousData = existingRecord.getDataJson() != null ? 
                new HashMap<>(existingRecord.getDataJson()) : new HashMap<>();

        // Validate data against entity definition
        validateRecordData(entity, request.getData(), false);
        
        // Validate using validation rules
        List<ValidationService.ValidationError> validationErrors = validationService.validateRecord(
                entityId, request.getData(), "UPDATE", previousData);
        if (!validationErrors.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation failed: ");
            for (ValidationService.ValidationError error : validationErrors) {
                if (error.getPropertyName() != null) {
                    errorMessage.append(error.getPropertyName()).append(": ");
                }
                errorMessage.append(error.getMessage()).append("; ");
            }
            throw new IllegalArgumentException(errorMessage.toString());
        }

        // Update record
        existingRecord.setDataJson(request.getData());
        // State should be managed through workflow transitions, not direct updates
        // Only allow state updates if no workflow is assigned, or if explicitly allowed
        if (request.getState() != null && (entity.getWorkflowId() == null || entity.getWorkflowId().isEmpty())) {
            existingRecord.setState(request.getState());
        }
        existingRecord.setUpdatedBy(user);

        EntityRecord record = entityRecordRepository.save(existingRecord);

        // Re-index in OpenSearch (async, non-blocking)
        try {
            searchIndexingService.indexRecord(entity, record);
        } catch (Exception e) {
            logger.warn("Failed to re-index record in search: entityId={}, recordId={}", 
                    entityId, recordId, e);
            // Don't fail the operation if indexing fails
        }

        // Audit log
        logger.info("Entity record updated: entityId={}, recordId={}, userId={}", 
                entityId, recordId, user.getUserId());

        return buildRecordDTO(record);
    }

    /**
     * Soft delete an entity record.
     */
    @Transactional
    public void deleteRecord(String entityId, UUID recordId, Authentication authentication) {
        // Get record
        EntityRecord record = entityRecordRepository.findByEntityIdAndRecordId(entityId, recordId)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + recordId));

        // Get current user
        User user = getCurrentUser(authentication);

        // TODO: Authorization check - verify user has ENTITY_DELETE permission for this entity
        // For now, just require authentication

        // Soft delete
        record.setDeletedAt(OffsetDateTime.now());
        record.setUpdatedBy(user);

        entityRecordRepository.save(record);

        // Remove from search index
        try {
            searchIndexingService.removeRecord(entityId, recordId);
        } catch (Exception e) {
            logger.warn("Failed to remove record from search index: entityId={}, recordId={}", 
                    entityId, recordId, e);
            // Don't fail the operation if indexing fails
        }

        // Audit log
        logger.info("Entity record deleted: entityId={}, recordId={}, userId={}", 
                entityId, recordId, user.getUserId());
    }

    /**
     * Validate record data against entity definition.
     */
    private void validateRecordData(EntityDefinition entity, Map<String, Object> data, boolean isCreate) {
        if (data == null) {
            throw new IllegalArgumentException("Record data cannot be null");
        }

        List<String> errors = new ArrayList<>();

        // Validate each property
        for (PropertyDefinition property : entity.getProperties()) {
            String propertyName = property.getPropertyName();
            Object value = data.get(propertyName);

            // Check if this is a master-detail field
            boolean isMasterDetail = false;
            if (property.getMetadataJson() != null) {
                Object isDetailEntityArray = property.getMetadataJson().get("isDetailEntityArray");
                isMasterDetail = isDetailEntityArray instanceof Boolean && (Boolean) isDetailEntityArray;
            }

            // Check required fields
            if (property.getRequired()) {
                if (value == null) {
                    errors.add("Required field '" + (property.getLabel() != null ? property.getLabel() : propertyName) + "' is missing");
                    continue;
                }
                // For master-detail fields, check if array is empty
                if (isMasterDetail && value instanceof java.util.List && ((java.util.List<?>) value).isEmpty()) {
                    errors.add("Required field '" + (property.getLabel() != null ? property.getLabel() : propertyName) + "' must have at least one item");
                    continue;
                }
                // For string fields, check if empty
                if (!isMasterDetail && value instanceof String && ((String) value).trim().isEmpty()) {
                    errors.add("Required field '" + (property.getLabel() != null ? property.getLabel() : propertyName) + "' cannot be empty");
                    continue;
                }
            }

            // Skip validation if value is null and field is not required
            if (value == null) {
                continue;
            }

            // Handle master-detail fields (they're stored as arrays but property type is string)
            if (isMasterDetail) {
                // Master-detail fields should be arrays
                if (!(value instanceof java.util.List)) {
                    errors.add("Field '" + (property.getLabel() != null ? property.getLabel() : propertyName) + "' must be an array");
                }
                // TODO: Could validate each line item against the detail entity definition
                continue; // Skip regular type validation for master-detail fields
            }

            // Type validation
            validatePropertyType(property, value, errors);
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + String.join("; ", errors));
        }
    }

    /**
     * Validate property type.
     */
    private void validatePropertyType(PropertyDefinition property, Object value, List<String> errors) {
        String propertyName = property.getPropertyName();
        String propertyType = property.getPropertyType();

        try {
            switch (propertyType) {
                case "string":
                    if (!(value instanceof String)) {
                        errors.add("Field '" + propertyName + "' must be a string");
                    }
                    break;
                case "number":
                    if (!(value instanceof Number)) {
                        errors.add("Field '" + propertyName + "' must be a number");
                    }
                    break;
                case "date":
                    // Accept string dates or timestamps
                    if (!(value instanceof String) && !(value instanceof Number)) {
                        errors.add("Field '" + propertyName + "' must be a date");
                    }
                    break;
                case "boolean":
                    if (!(value instanceof Boolean)) {
                        errors.add("Field '" + propertyName + "' must be a boolean");
                    }
                    break;
                case "singleSelect":
                    // Accept string or number
                    if (!(value instanceof String) && !(value instanceof Number)) {
                        errors.add("Field '" + propertyName + "' must be a single select value");
                    }
                    break;
                case "multiSelect":
                    if (!(value instanceof List)) {
                        errors.add("Field '" + propertyName + "' must be an array");
                    }
                    break;
                case "reference":
                    // Accept string (UUID) or object with id
                    if (!(value instanceof String) && !(value instanceof Map)) {
                        errors.add("Field '" + propertyName + "' must be a reference");
                    }
                    break;
                case "calculated":
                    // Calculated fields are read-only, should not be in create/update data
                    errors.add("Field '" + propertyName + "' is calculated and cannot be set");
                    break;
            }
        } catch (Exception e) {
            errors.add("Error validating field '" + propertyName + "': " + e.getMessage());
        }
    }

    /**
     * Get current user from authentication.
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("User not authenticated - authentication is null");
        }
        
        if (!(authentication.getPrincipal() instanceof UserDetails)) {
            throw new IllegalStateException("Invalid authentication principal type: " + 
                (authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getName() : "null"));
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        if (username == null || username.isEmpty()) {
            throw new IllegalStateException("Username is null or empty in authentication");
        }
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
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

    // Request DTOs
    public static class CreateRecordRequest {
        private Map<String, Object> data;
        private String state;

        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
    }

    public static class UpdateRecordRequest {
        private Map<String, Object> data;
        private String state;

        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
    }
}
