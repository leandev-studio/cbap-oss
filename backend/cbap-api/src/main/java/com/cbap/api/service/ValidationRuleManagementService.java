package com.cbap.api.service;

import com.cbap.persistence.entity.EntityDefinition;
import com.cbap.persistence.entity.User;
import com.cbap.persistence.entity.ValidationRule;
import com.cbap.persistence.repository.EntityDefinitionRepository;
import com.cbap.persistence.repository.UserRepository;
import com.cbap.persistence.repository.ValidationRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for validation rule management (admin only).
 */
@Service
public class ValidationRuleManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationRuleManagementService.class);

    private final ValidationRuleRepository validationRuleRepository;
    private final EntityDefinitionRepository entityDefinitionRepository;
    private final UserRepository userRepository;

    public ValidationRuleManagementService(
            ValidationRuleRepository validationRuleRepository,
            EntityDefinitionRepository entityDefinitionRepository,
            UserRepository userRepository) {
        this.validationRuleRepository = validationRuleRepository;
        this.entityDefinitionRepository = entityDefinitionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get all validation rules for an entity.
     */
    @Transactional(readOnly = true)
    public List<ValidationRule> getValidationRulesByEntity(String entityId) {
        return validationRuleRepository.findByEntityId(entityId);
    }

    /**
     * Get a validation rule by ID.
     */
    @Transactional(readOnly = true)
    public ValidationRule getValidationRuleById(UUID validationId) {
        return validationRuleRepository.findById(validationId)
                .orElseThrow(() -> new IllegalArgumentException("Validation rule not found: " + validationId));
    }

    /**
     * Create a new validation rule.
     */
    @Transactional
    public ValidationRule createValidationRule(CreateValidationRuleRequest request, Authentication authentication) {
        EntityDefinition entity = entityDefinitionRepository.findById(request.getEntityId())
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + request.getEntityId()));

        User user = getCurrentUser(authentication);

        ValidationRule rule = new ValidationRule();
        rule.setEntity(entity);
        rule.setPropertyName(request.getPropertyName());
        rule.setRuleName(request.getRuleName());
        rule.setDescription(request.getDescription());
        rule.setScope(ValidationRule.ValidationScope.valueOf(request.getScope().toUpperCase()));
        rule.setRuleType(ValidationRule.RuleType.valueOf(request.getRuleType().toUpperCase()));
        rule.setExpression(request.getExpression());
        rule.setErrorMessage(request.getErrorMessage());
        rule.setErrorMessageKey(request.getErrorMessageKey());
        rule.setTriggerEvents(request.getTriggerEvents());
        rule.setConditionsJson(request.getConditionsJson());
        rule.setMetadataJson(request.getMetadataJson());
        rule.setCreatedBy(user);

        return validationRuleRepository.save(rule);
    }

    /**
     * Update a validation rule.
     */
    @Transactional
    public ValidationRule updateValidationRule(UUID validationId, UpdateValidationRuleRequest request) {
        ValidationRule rule = getValidationRuleById(validationId);

        if (request.getRuleName() != null) {
            rule.setRuleName(request.getRuleName());
        }
        if (request.getDescription() != null) {
            rule.setDescription(request.getDescription());
        }
        if (request.getScope() != null) {
            rule.setScope(ValidationRule.ValidationScope.valueOf(request.getScope().toUpperCase()));
        }
        if (request.getRuleType() != null) {
            rule.setRuleType(ValidationRule.RuleType.valueOf(request.getRuleType().toUpperCase()));
        }
        if (request.getExpression() != null) {
            rule.setExpression(request.getExpression());
        }
        if (request.getErrorMessage() != null) {
            rule.setErrorMessage(request.getErrorMessage());
        }
        if (request.getErrorMessageKey() != null) {
            rule.setErrorMessageKey(request.getErrorMessageKey());
        }
        if (request.getTriggerEvents() != null) {
            rule.setTriggerEvents(request.getTriggerEvents());
        }
        if (request.getConditionsJson() != null) {
            rule.setConditionsJson(request.getConditionsJson());
        }
        if (request.getMetadataJson() != null) {
            rule.setMetadataJson(request.getMetadataJson());
        }

        return validationRuleRepository.save(rule);
    }

    /**
     * Delete a validation rule.
     */
    @Transactional
    public void deleteValidationRule(UUID validationId) {
        ValidationRule rule = getValidationRuleById(validationId);
        validationRuleRepository.delete(rule);
    }

    /**
     * Get current user from authentication.
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new RuntimeException("User not authenticated");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Request DTOs
    public static class CreateValidationRuleRequest {
        private String entityId;
        private String propertyName;
        private String ruleName;
        private String description;
        private String scope;
        private String ruleType;
        private String expression;
        private String errorMessage;
        private String errorMessageKey;
        private List<String> triggerEvents;
        private java.util.Map<String, Object> conditionsJson;
        private java.util.Map<String, Object> metadataJson;

        // Getters and setters
        public String getEntityId() { return entityId; }
        public void setEntityId(String entityId) { this.entityId = entityId; }
        public String getPropertyName() { return propertyName; }
        public void setPropertyName(String propertyName) { this.propertyName = propertyName; }
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
        public String getRuleType() { return ruleType; }
        public void setRuleType(String ruleType) { this.ruleType = ruleType; }
        public String getExpression() { return expression; }
        public void setExpression(String expression) { this.expression = expression; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public String getErrorMessageKey() { return errorMessageKey; }
        public void setErrorMessageKey(String errorMessageKey) { this.errorMessageKey = errorMessageKey; }
        public List<String> getTriggerEvents() { return triggerEvents; }
        public void setTriggerEvents(List<String> triggerEvents) { this.triggerEvents = triggerEvents; }
        public java.util.Map<String, Object> getConditionsJson() { return conditionsJson; }
        public void setConditionsJson(java.util.Map<String, Object> conditionsJson) { this.conditionsJson = conditionsJson; }
        public java.util.Map<String, Object> getMetadataJson() { return metadataJson; }
        public void setMetadataJson(java.util.Map<String, Object> metadataJson) { this.metadataJson = metadataJson; }
    }

    public static class UpdateValidationRuleRequest {
        private String ruleName;
        private String description;
        private String scope;
        private String ruleType;
        private String expression;
        private String errorMessage;
        private String errorMessageKey;
        private List<String> triggerEvents;
        private java.util.Map<String, Object> conditionsJson;
        private java.util.Map<String, Object> metadataJson;

        // Getters and setters
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
        public String getRuleType() { return ruleType; }
        public void setRuleType(String ruleType) { this.ruleType = ruleType; }
        public String getExpression() { return expression; }
        public void setExpression(String expression) { this.expression = expression; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public String getErrorMessageKey() { return errorMessageKey; }
        public void setErrorMessageKey(String errorMessageKey) { this.errorMessageKey = errorMessageKey; }
        public List<String> getTriggerEvents() { return triggerEvents; }
        public void setTriggerEvents(List<String> triggerEvents) { this.triggerEvents = triggerEvents; }
        public java.util.Map<String, Object> getConditionsJson() { return conditionsJson; }
        public void setConditionsJson(java.util.Map<String, Object> conditionsJson) { this.conditionsJson = conditionsJson; }
        public java.util.Map<String, Object> getMetadataJson() { return metadataJson; }
        public void setMetadataJson(java.util.Map<String, Object> metadataJson) { this.metadataJson = metadataJson; }
    }
}
