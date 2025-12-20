package com.cbap.search.service;

import com.cbap.persistence.entity.EntityDefinition;
import com.cbap.persistence.entity.EntityRecord;
import com.cbap.persistence.entity.PropertyDefinition;
import com.cbap.persistence.repository.EntityDefinitionRepository;
import com.cbap.persistence.repository.EntityRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for extracting denormalized fields from entity records for indexing.
 * 
 * Only properties marked with indexable=true in metadata_json are indexed for search.
 * For reference fields, indexes the display value from the referenced record.
 */
@Service
public class DenormalizationService {

    private static final Logger logger = LoggerFactory.getLogger(DenormalizationService.class);
    
    private final EntityRecordRepository entityRecordRepository;
    private final EntityDefinitionRepository entityDefinitionRepository;

    public DenormalizationService(
            EntityRecordRepository entityRecordRepository,
            EntityDefinitionRepository entityDefinitionRepository) {
        this.entityRecordRepository = entityRecordRepository;
        this.entityDefinitionRepository = entityDefinitionRepository;
    }

    /**
     * Extract denormalized fields from a record for indexing.
     * 
     * @param entity The entity definition
     * @param recordData The record data (JSONB)
     * @return Map of denormalized fields ready for indexing
     */
    public Map<String, Object> extractDenormalizedFields(EntityDefinition entity, Map<String, Object> recordData) {
        Map<String, Object> indexedFields = new HashMap<>();

        // Add metadata fields
        indexedFields.put("entityId", entity.getEntityId());
        indexedFields.put("entityName", entity.getName());
        indexedFields.put("schemaVersion", entity.getSchemaVersion());
        
        logger.debug("Extracting indexable fields for entity: entityId={}, recordDataKeys={}", 
                entity.getEntityId(), recordData != null ? recordData.keySet() : "null");

        // Extract indexable properties (check metadata_json for indexable flag)
        if (entity.getProperties() != null) {
            for (PropertyDefinition property : entity.getProperties()) {
                // Check if property is marked as indexable in metadata
                boolean isIndexable = false;
                if (property.getMetadataJson() != null) {
                    Object indexableObj = property.getMetadataJson().get("indexable");
                    if (indexableObj instanceof Boolean) {
                        isIndexable = (Boolean) indexableObj;
                    }
                }
                
                // Fallback to denormalize flag for backward compatibility
                if (!isIndexable) {
                    isIndexable = Boolean.TRUE.equals(property.getDenormalize());
                }
                
                if (isIndexable) {
                    String propertyName = property.getPropertyName();
                    Object value = recordData != null ? recordData.get(propertyName) : null;
                    
                    logger.debug("Indexing property: entityId={}, propertyName={}, propertyType={}, hasValue={}", 
                            entity.getEntityId(), propertyName, property.getPropertyType(), value != null);

                    if (value != null) {
                        // Handle different property types
                        String propertyType = property.getPropertyType();
                        
                        switch (propertyType) {
                            case "string":
                            case "number":
                            case "boolean":
                            case "date":
                                // Direct value indexing
                                indexedFields.put(propertyName, value);
                                break;
                            
                            case "singleSelect":
                                // Index as string
                                indexedFields.put(propertyName, String.valueOf(value));
                                break;
                            
                            case "multiSelect":
                                // Index as array of strings
                                if (value instanceof Iterable) {
                                    indexedFields.put(propertyName, value);
                                } else {
                                    indexedFields.put(propertyName, String.valueOf(value));
                                }
                                break;
                            
                            case "reference":
                                // Index reference ID
                                indexedFields.put(propertyName + "_id", value);
                                
                                // Also index the display value from the referenced record
                                EntityDefinition referenceEntity = property.getReferenceEntity();
                                if (referenceEntity != null && referenceEntity.getEntityId() != null) {
                                    try {
                                        String referenceId = value instanceof String 
                                                ? (String) value 
                                                : value.toString();
                                        
                                        // Fetch the referenced record
                                        UUID referenceUuid = UUID.fromString(referenceId);
                                        EntityRecord referencedRecord = entityRecordRepository
                                                .findByEntityIdAndRecordId(referenceEntity.getEntityId(), referenceUuid)
                                                .orElse(null);
                                        
                                        if (referencedRecord != null && referencedRecord.getDataJson() != null) {
                                            // Try common field names to get display value (name, companyName, title, etc.)
                                            String[] commonFields = {"name", "companyName", "title", "label"};
                                            String displayValue = null;
                                            
                                            for (String field : commonFields) {
                                                Object fieldValue = referencedRecord.getDataJson().get(field);
                                                if (fieldValue != null) {
                                                    displayValue = String.valueOf(fieldValue);
                                                    break;
                                                }
                                            }
                                            
                                            // If found, index the display value
                                            if (displayValue != null) {
                                                // Index as the property name itself for easier searching
                                                indexedFields.put(propertyName, displayValue);
                                            }
                                        }
                                    } catch (Exception e) {
                                        // Log but don't fail indexing if display value resolution fails
                                        logger.debug("Could not resolve display value for reference: property={}, referenceId={}, error={}", 
                                                propertyName, value, e.getMessage());
                                    }
                                }
                                break;
                            
                            case "calculated":
                                // Index calculated values
                                indexedFields.put(propertyName, value);
                                break;
                            
                            default:
                                // Fallback: index as string
                                indexedFields.put(propertyName, String.valueOf(value));
                        }
                    }
                }
            }
        }

        logger.debug("Extracted indexed fields for entity: entityId={}, fieldCount={}, fields={}", 
                entity.getEntityId(), indexedFields.size(), indexedFields.keySet());
        
        return indexedFields;
    }
}
