package com.cbap.api.service;

import com.cbap.api.service.expression.ExpressionEvaluator;
import com.cbap.persistence.entity.EntityDefinition;
import com.cbap.persistence.entity.EntityRecord;
import com.cbap.persistence.entity.PropertyDefinition;
import com.cbap.persistence.entity.ValidationRule;
import com.cbap.persistence.repository.EntityDefinitionRepository;
import com.cbap.persistence.repository.EntityRecordRepository;
import com.cbap.persistence.repository.ValidationRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for validation rule evaluation.
 */
@Service
public class ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);

    private final ValidationRuleRepository validationRuleRepository;
    private final EntityDefinitionRepository entityDefinitionRepository;
    private final EntityRecordRepository entityRecordRepository;

    public ValidationService(
            ValidationRuleRepository validationRuleRepository,
            EntityDefinitionRepository entityDefinitionRepository,
            EntityRecordRepository entityRecordRepository) {
        this.validationRuleRepository = validationRuleRepository;
        this.entityDefinitionRepository = entityDefinitionRepository;
        this.entityRecordRepository = entityRecordRepository;
    }

    /**
     * Validate a record against all applicable validation rules.
     * 
     * @param entityId The entity ID
     * @param recordData The record data to validate
     * @param triggerEvent The event that triggered validation (CREATE, UPDATE, DELETE, TRANSITION)
     * @param previousRecordData Previous record data (for UPDATE/TRANSITION)
     * @return List of validation errors (empty if valid)
     */
    @Transactional(readOnly = true)
    public List<ValidationError> validateRecord(String entityId, Map<String, Object> recordData, String triggerEvent, Map<String, Object> previousRecordData) {
        List<ValidationError> errors = new ArrayList<>();

        EntityDefinition entity = entityDefinitionRepository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityId));

        // Get all validation rules for this entity
        List<ValidationRule> rules = validationRuleRepository.findByEntityId(entityId);

        // Build evaluation context
        Map<String, Object> context = buildEvaluationContext(entity, recordData, previousRecordData, triggerEvent);

        // Validate field-level rules
        for (PropertyDefinition property : entity.getProperties()) {
            List<ValidationRule> fieldRules = validationRuleRepository.findByEntityIdAndPropertyName(entityId, property.getPropertyName());
            for (ValidationRule rule : fieldRules) {
                if (shouldEvaluateRule(rule, triggerEvent)) {
                    ValidationError error = evaluateFieldRule(rule, property, recordData, context);
                    if (error != null) {
                        errors.add(error);
                    }
                }
            }
        }

        // Validate entity-level rules
        List<ValidationRule> entityRules = validationRuleRepository.findEntityLevelRules(entityId);
        for (ValidationRule rule : entityRules) {
            if (shouldEvaluateRule(rule, triggerEvent)) {
                ValidationError error = evaluateEntityRule(rule, recordData, context);
                if (error != null) {
                    errors.add(error);
                }
            }
        }

        // Validate cross-entity rules
        List<ValidationRule> crossEntityRules = validationRuleRepository.findCrossEntityRules(entityId);
        for (ValidationRule rule : crossEntityRules) {
            if (shouldEvaluateRule(rule, triggerEvent)) {
                ValidationError error = evaluateCrossEntityRule(rule, entityId, recordData, context);
                if (error != null) {
                    errors.add(error);
                }
            }
        }

        return errors;
    }

    /**
     * Validate a field value against field-level rules.
     */
    @Transactional(readOnly = true)
    public List<ValidationError> validateField(String entityId, String propertyName, Object value, Map<String, Object> fullRecordData) {
        List<ValidationError> errors = new ArrayList<>();

        EntityDefinition entity = entityDefinitionRepository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityId));

        PropertyDefinition property = entity.getProperties().stream()
                .filter(p -> p.getPropertyName().equals(propertyName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyName));

        List<ValidationRule> rules = validationRuleRepository.findByEntityIdAndPropertyName(entityId, propertyName);

        Map<String, Object> context = buildEvaluationContext(entity, fullRecordData, null, "UPDATE");

        for (ValidationRule rule : rules) {
            ValidationError error = evaluateFieldRule(rule, property, fullRecordData, context);
            if (error != null) {
                errors.add(error);
            }
        }

        return errors;
    }

    /**
     * Check if a rule should be evaluated for the given trigger event.
     */
    private boolean shouldEvaluateRule(ValidationRule rule, String triggerEvent) {
        if (rule.getTriggerEvents() == null || rule.getTriggerEvents().isEmpty()) {
            return true; // No trigger events specified, evaluate always
        }
        return rule.getTriggerEvents().contains(triggerEvent);
    }

    /**
     * Evaluate a field-level validation rule.
     */
    private ValidationError evaluateFieldRule(ValidationRule rule, PropertyDefinition property, Map<String, Object> recordData, Map<String, Object> context) {
        Object value = recordData.get(property.getPropertyName());

        try {
            switch (rule.getRuleType()) {
                case REQUIRED:
                    if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                        return new ValidationError(
                                rule.getValidationId().toString(),
                                property.getPropertyName(),
                                rule.getErrorMessage() != null ? rule.getErrorMessage() : 
                                    (property.getLabel() != null ? property.getLabel() : property.getPropertyName()) + " is required",
                                rule.getErrorMessageKey(),
                                ValidationError.ErrorLevel.FIELD
                        );
                    }
                    break;

                case TYPE:
                    if (value != null && !isValidType(value, property.getPropertyType())) {
                        return new ValidationError(
                                rule.getValidationId().toString(),
                                property.getPropertyName(),
                                rule.getErrorMessage() != null ? rule.getErrorMessage() : 
                                    (property.getLabel() != null ? property.getLabel() : property.getPropertyName()) + " has invalid type",
                                rule.getErrorMessageKey(),
                                ValidationError.ErrorLevel.FIELD
                        );
                    }
                    break;

                case RANGE:
                    if (value != null) {
                        Map<String, Object> metadata = rule.getMetadataJson();
                        if (metadata != null) {
                            Double numValue = toNumber(value);
                            if (numValue != null) {
                                Object minObj = metadata.get("min");
                                Object maxObj = metadata.get("max");
                                if (minObj != null && numValue < toNumber(minObj)) {
                                    return new ValidationError(
                                            rule.getValidationId().toString(),
                                            property.getPropertyName(),
                                            rule.getErrorMessage() != null ? rule.getErrorMessage() : 
                                                (property.getLabel() != null ? property.getLabel() : property.getPropertyName()) + " is below minimum",
                                            rule.getErrorMessageKey(),
                                            ValidationError.ErrorLevel.FIELD
                                    );
                                }
                                if (maxObj != null && numValue > toNumber(maxObj)) {
                                    return new ValidationError(
                                            rule.getValidationId().toString(),
                                            property.getPropertyName(),
                                            rule.getErrorMessage() != null ? rule.getErrorMessage() : 
                                                (property.getLabel() != null ? property.getLabel() : property.getPropertyName()) + " is above maximum",
                                            rule.getErrorMessageKey(),
                                            ValidationError.ErrorLevel.FIELD
                                    );
                                }
                            }
                        }
                    }
                    break;

                case LENGTH:
                    if (value != null && value instanceof String) {
                        Map<String, Object> metadata = rule.getMetadataJson();
                        if (metadata != null) {
                            String strValue = (String) value;
                            Object minLengthObj = metadata.get("minLength");
                            Object maxLengthObj = metadata.get("maxLength");
                            if (minLengthObj != null && strValue.length() < toNumber(minLengthObj).intValue()) {
                                return new ValidationError(
                                        rule.getValidationId().toString(),
                                        property.getPropertyName(),
                                        rule.getErrorMessage() != null ? rule.getErrorMessage() : 
                                            (property.getLabel() != null ? property.getLabel() : property.getPropertyName()) + " is too short",
                                        rule.getErrorMessageKey(),
                                        ValidationError.ErrorLevel.FIELD
                                );
                            }
                            if (maxLengthObj != null && strValue.length() > toNumber(maxLengthObj).intValue()) {
                                return new ValidationError(
                                        rule.getValidationId().toString(),
                                        property.getPropertyName(),
                                        rule.getErrorMessage() != null ? rule.getErrorMessage() : 
                                            (property.getLabel() != null ? property.getLabel() : property.getPropertyName()) + " is too long",
                                        rule.getErrorMessageKey(),
                                        ValidationError.ErrorLevel.FIELD
                                );
                            }
                        }
                    }
                    break;

                case PATTERN:
                    if (value != null && value instanceof String) {
                        Map<String, Object> metadata = rule.getMetadataJson();
                        if (metadata != null) {
                            String pattern = (String) metadata.get("pattern");
                            if (pattern != null && !((String) value).matches(pattern)) {
                                return new ValidationError(
                                        rule.getValidationId().toString(),
                                        property.getPropertyName(),
                                        rule.getErrorMessage() != null ? rule.getErrorMessage() : 
                                            (property.getLabel() != null ? property.getLabel() : property.getPropertyName()) + " does not match required pattern",
                                        rule.getErrorMessageKey(),
                                        ValidationError.ErrorLevel.FIELD
                                );
                            }
                        }
                    }
                    break;

                case EXPRESSION:
                    if (rule.getExpression() != null) {
                        // Add the current field value to context
                        Map<String, Object> fieldContext = new HashMap<>(context);
                        fieldContext.put(property.getPropertyName(), value);
                        fieldContext.put("value", value);
                        
                        Boolean result = ExpressionEvaluator.evaluateBoolean(rule.getExpression(), fieldContext);
                        if (result == null || !result) {
                            return new ValidationError(
                                    rule.getValidationId().toString(),
                                    property.getPropertyName(),
                                    rule.getErrorMessage() != null ? rule.getErrorMessage() : 
                                        (property.getLabel() != null ? property.getLabel() : property.getPropertyName()) + " validation failed",
                                    rule.getErrorMessageKey(),
                                    ValidationError.ErrorLevel.FIELD
                            );
                        }
                    }
                    break;

                default:
                    logger.warn("Unsupported rule type: {}", rule.getRuleType());
            }
        } catch (Exception e) {
            logger.error("Error evaluating validation rule: ruleId={}, error={}", rule.getValidationId(), e.getMessage(), e);
            return new ValidationError(
                    rule.getValidationId().toString(),
                    property.getPropertyName(),
                    "Validation error: " + e.getMessage(),
                    null,
                    ValidationError.ErrorLevel.FIELD
            );
        }

        return null; // No error
    }

    /**
     * Evaluate an entity-level validation rule.
     */
    private ValidationError evaluateEntityRule(ValidationRule rule, Map<String, Object> recordData, Map<String, Object> context) {
        if (rule.getExpression() != null) {
            try {
                Boolean result = ExpressionEvaluator.evaluateBoolean(rule.getExpression(), context);
                if (result == null || !result) {
                    return new ValidationError(
                            rule.getValidationId().toString(),
                            null,
                            rule.getErrorMessage() != null ? rule.getErrorMessage() : "Entity validation failed",
                            rule.getErrorMessageKey(),
                            ValidationError.ErrorLevel.ENTITY
                    );
                }
            } catch (Exception e) {
                logger.error("Error evaluating entity rule: ruleId={}, error={}", rule.getValidationId(), e.getMessage(), e);
                return new ValidationError(
                        rule.getValidationId().toString(),
                        null,
                        "Validation error: " + e.getMessage(),
                        null,
                        ValidationError.ErrorLevel.ENTITY
                );
            }
        }
        return null;
    }

    /**
     * Evaluate a cross-entity validation rule.
     */
    private ValidationError evaluateCrossEntityRule(ValidationRule rule, String entityId, Map<String, Object> recordData, Map<String, Object> context) {
        // For cross-entity validation, we may need to fetch related entities
        // This is a placeholder - full implementation would resolve references
        if (rule.getExpression() != null) {
            try {
                Boolean result = ExpressionEvaluator.evaluateBoolean(rule.getExpression(), context);
                if (result == null || !result) {
                    return new ValidationError(
                            rule.getValidationId().toString(),
                            null,
                            rule.getErrorMessage() != null ? rule.getErrorMessage() : "Cross-entity validation failed",
                            rule.getErrorMessageKey(),
                            ValidationError.ErrorLevel.CROSS_ENTITY
                    );
                }
            } catch (Exception e) {
                logger.error("Error evaluating cross-entity rule: ruleId={}, error={}", rule.getValidationId(), e.getMessage(), e);
                return new ValidationError(
                        rule.getValidationId().toString(),
                        null,
                        "Validation error: " + e.getMessage(),
                        null,
                        ValidationError.ErrorLevel.CROSS_ENTITY
                );
            }
        }
        return null;
    }

    /**
     * Build evaluation context for expression evaluation.
     */
    private Map<String, Object> buildEvaluationContext(EntityDefinition entity, Map<String, Object> recordData, Map<String, Object> previousRecordData, String triggerEvent) {
        Map<String, Object> context = new HashMap<>();

        // Add all record data as context variables
        if (recordData != null) {
            context.putAll(recordData);
            context.put("this", recordData);
        }

        // Add previous record data
        if (previousRecordData != null) {
            context.put("previous", previousRecordData);
        }

        // Add trigger event
        context.put("triggerEvent", triggerEvent);

        // Add entity metadata
        context.put("entityId", entity.getEntityId());
        context.put("entityName", entity.getName());

        // TODO: Add user context, workflow context, etc.
        // context.put("currentUser", ...);
        // context.put("currentState", ...);

        return context;
    }

    /**
     * Check if a value is valid for the given property type.
     */
    private boolean isValidType(Object value, String propertyType) {
        switch (propertyType) {
            case "string":
                return value instanceof String;
            case "number":
                return value instanceof Number;
            case "date":
                return value instanceof String || value instanceof java.time.temporal.TemporalAccessor;
            case "boolean":
                return value instanceof Boolean;
            case "singleSelect":
            case "multiSelect":
                return value instanceof String || value instanceof List;
            case "reference":
                return value instanceof String; // UUID as string
            default:
                return true; // Unknown type, accept it
        }
    }

    private Double toNumber(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Validation error DTO.
     */
    public static class ValidationError {
        private String validationId;
        private String propertyName;
        private String message;
        private String messageKey;
        private ErrorLevel level;

        public ValidationError(String validationId, String propertyName, String message, String messageKey, ErrorLevel level) {
            this.validationId = validationId;
            this.propertyName = propertyName;
            this.message = message;
            this.messageKey = messageKey;
            this.level = level;
        }

        public enum ErrorLevel {
            FIELD,
            ENTITY,
            CROSS_ENTITY,
            WORKFLOW_TRANSITION
        }

        // Getters and Setters
        public String getValidationId() { return validationId; }
        public void setValidationId(String validationId) { this.validationId = validationId; }
        public String getPropertyName() { return propertyName; }
        public void setPropertyName(String propertyName) { this.propertyName = propertyName; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getMessageKey() { return messageKey; }
        public void setMessageKey(String messageKey) { this.messageKey = messageKey; }
        public ErrorLevel getLevel() { return level; }
        public void setLevel(ErrorLevel level) { this.level = level; }
    }
}
