package com.cbap.search.service;

import com.cbap.persistence.entity.EntityDefinition;
import com.cbap.persistence.entity.PropertyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for extracting denormalized fields from entity records for indexing.
 * 
 * Only properties marked with denormalize=true are indexed for search.
 */
@Service
public class DenormalizationService {

    private static final Logger logger = LoggerFactory.getLogger(DenormalizationService.class);

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

        // Extract denormalized properties
        if (entity.getProperties() != null) {
            for (PropertyDefinition property : entity.getProperties()) {
                if (Boolean.TRUE.equals(property.getDenormalize())) {
                    String propertyName = property.getPropertyName();
                    Object value = recordData != null ? recordData.get(propertyName) : null;

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
                                // Index reference ID and optionally resolved display value
                                indexedFields.put(propertyName + "_id", value);
                                // Note: Display value resolution would require fetching referenced record
                                // For now, we index the ID only
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

        return indexedFields;
    }
}
