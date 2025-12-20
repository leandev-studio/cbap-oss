package com.cbap.api.service;

import com.cbap.persistence.entity.EntityDefinition;
import com.cbap.persistence.repository.EntityDefinitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service for computing display values for search results based on entity metadata.
 */
@Service
public class SearchDisplayService {

    private static final Logger logger = LoggerFactory.getLogger(SearchDisplayService.class);

    private final EntityDefinitionRepository entityDefinitionRepository;

    public SearchDisplayService(EntityDefinitionRepository entityDefinitionRepository) {
        this.entityDefinitionRepository = entityDefinitionRepository;
    }

    /**
     * Compute display value for a search hit based on entity metadata.
     * 
     * @param entityId The entity ID
     * @param searchData The indexed search data (from OpenSearch)
     * @return Display value string, or null if not configured
     */
    @Transactional(readOnly = true)
    public String computeDisplayValue(String entityId, Map<String, Object> searchData) {
        if (entityId == null || searchData == null) {
            return null;
        }

        try {
            EntityDefinition entity = entityDefinitionRepository.findById(entityId).orElse(null);
            if (entity == null || entity.getMetadataJson() == null) {
                return null;
            }

            // Get searchDisplay configuration from entity metadata
            Object searchDisplayObj = entity.getMetadataJson().get("searchDisplay");
            if (searchDisplayObj == null) {
                return null;
            }

            String searchDisplay = searchDisplayObj.toString();
            if (searchDisplay.trim().isEmpty()) {
                return null;
            }

            // Handle multiple fields separated by "|"
            if (searchDisplay.contains("|")) {
                String[] fields = searchDisplay.split("\\|");
                StringBuilder displayValue = new StringBuilder();
                
                for (int i = 0; i < fields.length; i++) {
                    String field = fields[i].trim();
                    Object value = searchData.get(field);
                    
                    if (value != null) {
                        if (displayValue.length() > 0) {
                            displayValue.append(" | ");
                        }
                        displayValue.append(String.valueOf(value));
                    }
                }
                
                return displayValue.length() > 0 ? displayValue.toString() : null;
            } else {
                // Single field
                Object value = searchData.get(searchDisplay.trim());
                return value != null ? String.valueOf(value) : null;
            }

        } catch (Exception e) {
            logger.debug("Error computing display value for entity: entityId={}, error={}", 
                    entityId, e.getMessage());
            return null;
        }
    }
}
