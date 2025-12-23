package com.cbap.api.service;

import com.cbap.api.service.expression.ExpressionEvaluator;
import com.cbap.persistence.entity.EntityDefinition;
import com.cbap.persistence.entity.PropertyDefinition;
import com.cbap.persistence.entity.EntityRecord;
import com.cbap.persistence.repository.EntityRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for computing calculated field values based on metadata expressions.
 * 
 * This service evaluates calculated field expressions defined in property metadata
 * to compute field values dynamically.
 */
@Service
public class CalculatedFieldService {

    private static final Logger logger = LoggerFactory.getLogger(CalculatedFieldService.class);

    private final EntityRecordRepository entityRecordRepository;

    public CalculatedFieldService(EntityRecordRepository entityRecordRepository) {
        this.entityRecordRepository = entityRecordRepository;
    }

    /**
     * Compute calculated field values for a record based on metadata expressions.
     * 
     * @param entity The entity definition
     * @param recordData The record data (will be updated with calculated values)
     * @param parentData Optional parent record data (for master-detail relationships)
     */
    @Transactional(readOnly = true)
    public void computeCalculatedFields(EntityDefinition entity, Map<String, Object> recordData, Map<String, Object> parentData) {
        // Build evaluation context
        Map<String, Object> context = buildEvaluationContext(recordData, parentData);

        // Process each calculated property
        for (PropertyDefinition property : entity.getProperties()) {
            if (property.getPropertyType().equals("calculated") && property.getMetadataJson() != null) {
                Object expressionObj = property.getMetadataJson().get("expression");
                if (expressionObj instanceof String) {
                    String expression = (String) expressionObj;
                    
                    try {
                        // Evaluate the expression
                        Object result = ExpressionEvaluator.evaluate(expression, context);
                        
                        // Set the calculated value
                        recordData.put(property.getPropertyName(), result);
                        
                        logger.debug("Computed calculated field: {}.{} = {}", 
                                entity.getEntityId(), property.getPropertyName(), result);
                    } catch (ExpressionEvaluator.ExpressionEvaluationException e) {
                        logger.warn("Failed to compute calculated field: {}.{}, expression: {}, error: {}", 
                                entity.getEntityId(), property.getPropertyName(), expression, e.getMessage());
                        // Don't fail the operation, just log the warning
                    }
                }
            }
        }
    }

    /**
     * Build evaluation context for calculated field expressions.
     */
    private Map<String, Object> buildEvaluationContext(Map<String, Object> recordData, Map<String, Object> parentData) {
        Map<String, Object> context = new HashMap<>();
        
        // Add record data as context variables
        if (recordData != null) {
            context.putAll(recordData);
            context.put("this", recordData);
        }
        
        // Add parent data if available (for master-detail relationships)
        if (parentData != null) {
            context.put("parent", parentData);
            context.put("$parent", parentData); // Also available with $ prefix
        }
        
        // Add helper function for cross-entity lookups
        // Note: This is a simplified implementation. Full implementation would use measures.
        context.put("lookupCountryTax", new java.util.function.Function<String, Double>() {
            @Override
            public Double apply(String customerId) {
                try {
                    if (customerId == null) return 0.0;
                    EntityRecord customerRecord = entityRecordRepository.findByRecordId(UUID.fromString(customerId))
                            .orElse(null);
                    if (customerRecord != null && customerRecord.getDataJson() != null) {
                        String countryId = (String) customerRecord.getDataJson().get("country");
                        if (countryId != null) {
                            EntityRecord countryRecord = entityRecordRepository.findByRecordId(UUID.fromString(countryId))
                                    .orElse(null);
                            if (countryRecord != null && countryRecord.getDataJson() != null) {
                                Object tax = countryRecord.getDataJson().get("federalTax");
                                if (tax instanceof Number) {
                                    return ((Number) tax).doubleValue();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Failed to lookup country tax for customer: {}", customerId, e);
                }
                return 0.0;
            }
        });
        
        return context;
    }
}
