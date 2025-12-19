package com.cbap.api.service;

import com.cbap.persistence.entity.EntityDefinition;
import com.cbap.persistence.entity.PropertyDefinition;
import com.cbap.persistence.entity.User;
import com.cbap.persistence.repository.EntityDefinitionRepository;
import com.cbap.persistence.repository.PropertyDefinitionRepository;
import com.cbap.persistence.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for entity metadata operations.
 */
@Service
public class EntityMetadataService {

    private final EntityDefinitionRepository entityDefinitionRepository;
    private final PropertyDefinitionRepository propertyDefinitionRepository;
    private final UserRepository userRepository;

    public EntityMetadataService(
            EntityDefinitionRepository entityDefinitionRepository,
            PropertyDefinitionRepository propertyDefinitionRepository,
            UserRepository userRepository) {
        this.entityDefinitionRepository = entityDefinitionRepository;
        this.propertyDefinitionRepository = propertyDefinitionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get all entity definitions.
     */
    @Transactional(readOnly = true)
    public List<EntityDefinitionDTO> getAllEntities() {
        List<EntityDefinition> entities = entityDefinitionRepository.findAllWithProperties();
        return entities.stream()
                .map(this::buildEntityDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get entity definition by ID.
     */
    @Transactional(readOnly = true)
    public EntityDefinitionDTO getEntityById(String entityId) {
        EntityDefinition entity = entityDefinitionRepository.findByEntityIdWithProperties(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityId));
        return buildEntityDTO(entity);
    }

    /**
     * Create a new entity definition (admin only).
     */
    @Transactional
    public EntityDefinitionDTO createEntity(CreateEntityRequest request, Authentication authentication) {
        // Check if entity ID already exists
        if (entityDefinitionRepository.existsByEntityId(request.getEntityId())) {
            throw new IllegalArgumentException("Entity ID already exists: " + request.getEntityId());
        }

        User user = getCurrentUser(authentication);

        // Create entity definition
        EntityDefinition entity = new EntityDefinition();
        entity.setEntityId(request.getEntityId());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setSchemaVersion(request.getSchemaVersion() != null ? request.getSchemaVersion() : 1);
        entity.setScreenVersion(request.getScreenVersion() != null ? request.getScreenVersion() : 1);
        entity.setWorkflowId(request.getWorkflowId());
        entity.setAuthorizationModel(request.getAuthorizationModel());
        if (request.getScope() != null) {
            entity.setScope(EntityDefinition.Scope.valueOf(request.getScope().toUpperCase()));
        }
        entity.setMetadataJson(request.getMetadataJson());
        entity.setCreatedBy(user);

        entity = entityDefinitionRepository.save(entity);

        // Create properties if provided
        if (request.getProperties() != null) {
            for (CreatePropertyRequest propRequest : request.getProperties()) {
                PropertyDefinition property = new PropertyDefinition();
                property.setEntity(entity);
                property.setPropertyName(propRequest.getPropertyName());
                property.setPropertyType(propRequest.getPropertyType().toLowerCase());
                property.setLabel(propRequest.getLabel());
                property.setLabelKey(propRequest.getLabelKey());
                property.setRequired(propRequest.getRequired() != null ? propRequest.getRequired() : false);
                property.setReadOnly(propRequest.getReadOnly() != null ? propRequest.getReadOnly() : false);
                property.setDenormalize(propRequest.getDenormalize() != null ? propRequest.getDenormalize() : false);
                if (propRequest.getReferenceEntityId() != null) {
                    EntityDefinition refEntity = entityDefinitionRepository.findById(propRequest.getReferenceEntityId())
                            .orElseThrow(() -> new IllegalArgumentException("Reference entity not found: " + propRequest.getReferenceEntityId()));
                    property.setReferenceEntity(refEntity);
                }
                property.setCalculationExpression(propRequest.getCalculationExpression());
                property.setMetadataJson(propRequest.getMetadataJson());
                propertyDefinitionRepository.save(property);
            }
        }

        // Reload with properties
        entity = entityDefinitionRepository.findByEntityIdWithProperties(entity.getEntityId())
                .orElse(entity);

        return buildEntityDTO(entity);
    }

    /**
     * Update an entity definition (admin only).
     */
    @Transactional
    public EntityDefinitionDTO updateEntity(String entityId, UpdateEntityRequest request) {
        EntityDefinition entity = entityDefinitionRepository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + entityId));

        // Update fields
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getSchemaVersion() != null) {
            entity.setSchemaVersion(request.getSchemaVersion());
        }
        if (request.getScreenVersion() != null) {
            entity.setScreenVersion(request.getScreenVersion());
        }
        if (request.getWorkflowId() != null) {
            entity.setWorkflowId(request.getWorkflowId());
        }
        if (request.getAuthorizationModel() != null) {
            entity.setAuthorizationModel(request.getAuthorizationModel());
        }
        if (request.getScope() != null) {
            entity.setScope(EntityDefinition.Scope.valueOf(request.getScope().toUpperCase()));
        }
        if (request.getMetadataJson() != null) {
            entity.setMetadataJson(request.getMetadataJson());
        }

        entity = entityDefinitionRepository.save(entity);

        // Reload with properties
        entity = entityDefinitionRepository.findByEntityIdWithProperties(entity.getEntityId())
                .orElse(entity);

        return buildEntityDTO(entity);
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

    /**
     * Build EntityDefinition DTO.
     */
    private EntityDefinitionDTO buildEntityDTO(EntityDefinition entity) {
        List<PropertyDefinitionDTO> propertyDTOs = entity.getProperties() != null
                ? entity.getProperties().stream()
                        .map(this::buildPropertyDTO)
                        .collect(Collectors.toList())
                : List.of();

        return new EntityDefinitionDTO(
                entity.getEntityId(),
                entity.getName(),
                entity.getDescription(),
                entity.getSchemaVersion(),
                entity.getScreenVersion(),
                entity.getWorkflowId(),
                entity.getAuthorizationModel(),
                entity.getScope() != null ? entity.getScope().name() : null,
                entity.getMetadataJson(),
                propertyDTOs
        );
    }

    /**
     * Build PropertyDefinition DTO.
     */
    private PropertyDefinitionDTO buildPropertyDTO(PropertyDefinition property) {
        return new PropertyDefinitionDTO(
                property.getPropertyId().toString(),
                property.getPropertyName(),
                property.getPropertyType(),
                property.getLabel(),
                property.getLabelKey(),
                property.getRequired(),
                property.getReadOnly(),
                property.getDenormalize(),
                property.getReferenceEntity() != null ? property.getReferenceEntity().getEntityId() : null,
                property.getCalculationExpression(),
                property.getMetadataJson()
        );
    }

    // DTOs
    public static class EntityDefinitionDTO {
        private final String entityId;
        private final String name;
        private final String description;
        private final Integer schemaVersion;
        private final Integer screenVersion;
        private final String workflowId;
        private final String authorizationModel;
        private final String scope;
        private final java.util.Map<String, Object> metadataJson;
        private final List<PropertyDefinitionDTO> properties;

        public EntityDefinitionDTO(String entityId, String name, String description, Integer schemaVersion,
                                  Integer screenVersion, String workflowId, String authorizationModel, String scope,
                                  java.util.Map<String, Object> metadataJson, List<PropertyDefinitionDTO> properties) {
            this.entityId = entityId;
            this.name = name;
            this.description = description;
            this.schemaVersion = schemaVersion;
            this.screenVersion = screenVersion;
            this.workflowId = workflowId;
            this.authorizationModel = authorizationModel;
            this.scope = scope;
            this.metadataJson = metadataJson;
            this.properties = properties;
        }

        // Getters
        public String getEntityId() { return entityId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Integer getSchemaVersion() { return schemaVersion; }
        public Integer getScreenVersion() { return screenVersion; }
        public String getWorkflowId() { return workflowId; }
        public String getAuthorizationModel() { return authorizationModel; }
        public String getScope() { return scope; }
        public java.util.Map<String, Object> getMetadataJson() { return metadataJson; }
        public List<PropertyDefinitionDTO> getProperties() { return properties; }
    }

    public static class PropertyDefinitionDTO {
        private final String propertyId;
        private final String propertyName;
        private final String propertyType;
        private final String label;
        private final String labelKey;
        private final Boolean required;
        private final Boolean readOnly;
        private final Boolean denormalize;
        private final String referenceEntityId;
        private final String calculationExpression;
        private final java.util.Map<String, Object> metadataJson;

        public PropertyDefinitionDTO(String propertyId, String propertyName, String propertyType, String label,
                                     String labelKey, Boolean required, Boolean readOnly, Boolean denormalize,
                                     String referenceEntityId, String calculationExpression,
                                     java.util.Map<String, Object> metadataJson) {
            this.propertyId = propertyId;
            this.propertyName = propertyName;
            this.propertyType = propertyType;
            this.label = label;
            this.labelKey = labelKey;
            this.required = required;
            this.readOnly = readOnly;
            this.denormalize = denormalize;
            this.referenceEntityId = referenceEntityId;
            this.calculationExpression = calculationExpression;
            this.metadataJson = metadataJson;
        }

        // Getters
        public String getPropertyId() { return propertyId; }
        public String getPropertyName() { return propertyName; }
        public String getPropertyType() { return propertyType; }
        public String getLabel() { return label; }
        public String getLabelKey() { return labelKey; }
        public Boolean getRequired() { return required; }
        public Boolean getReadOnly() { return readOnly; }
        public Boolean getDenormalize() { return denormalize; }
        public String getReferenceEntityId() { return referenceEntityId; }
        public String getCalculationExpression() { return calculationExpression; }
        public java.util.Map<String, Object> getMetadataJson() { return metadataJson; }
    }

    // Request DTOs
    public static class CreateEntityRequest {
        private String entityId;
        private String name;
        private String description;
        private Integer schemaVersion;
        private Integer screenVersion;
        private String workflowId;
        private String authorizationModel;
        private String scope;
        private java.util.Map<String, Object> metadataJson;
        private List<CreatePropertyRequest> properties;

        // Getters and setters
        public String getEntityId() { return entityId; }
        public void setEntityId(String entityId) { this.entityId = entityId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getSchemaVersion() { return schemaVersion; }
        public void setSchemaVersion(Integer schemaVersion) { this.schemaVersion = schemaVersion; }
        public Integer getScreenVersion() { return screenVersion; }
        public void setScreenVersion(Integer screenVersion) { this.screenVersion = screenVersion; }
        public String getWorkflowId() { return workflowId; }
        public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
        public String getAuthorizationModel() { return authorizationModel; }
        public void setAuthorizationModel(String authorizationModel) { this.authorizationModel = authorizationModel; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
        public java.util.Map<String, Object> getMetadataJson() { return metadataJson; }
        public void setMetadataJson(java.util.Map<String, Object> metadataJson) { this.metadataJson = metadataJson; }
        public List<CreatePropertyRequest> getProperties() { return properties; }
        public void setProperties(List<CreatePropertyRequest> properties) { this.properties = properties; }
    }

    public static class CreatePropertyRequest {
        private String propertyName;
        private String propertyType;
        private String label;
        private String labelKey;
        private Boolean required;
        private Boolean readOnly;
        private Boolean denormalize;
        private String referenceEntityId;
        private String calculationExpression;
        private java.util.Map<String, Object> metadataJson;

        // Getters and setters
        public String getPropertyName() { return propertyName; }
        public void setPropertyName(String propertyName) { this.propertyName = propertyName; }
        public String getPropertyType() { return propertyType; }
        public void setPropertyType(String propertyType) { this.propertyType = propertyType; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getLabelKey() { return labelKey; }
        public void setLabelKey(String labelKey) { this.labelKey = labelKey; }
        public Boolean getRequired() { return required; }
        public void setRequired(Boolean required) { this.required = required; }
        public Boolean getReadOnly() { return readOnly; }
        public void setReadOnly(Boolean readOnly) { this.readOnly = readOnly; }
        public Boolean getDenormalize() { return denormalize; }
        public void setDenormalize(Boolean denormalize) { this.denormalize = denormalize; }
        public String getReferenceEntityId() { return referenceEntityId; }
        public void setReferenceEntityId(String referenceEntityId) { this.referenceEntityId = referenceEntityId; }
        public String getCalculationExpression() { return calculationExpression; }
        public void setCalculationExpression(String calculationExpression) { this.calculationExpression = calculationExpression; }
        public java.util.Map<String, Object> getMetadataJson() { return metadataJson; }
        public void setMetadataJson(java.util.Map<String, Object> metadataJson) { this.metadataJson = metadataJson; }
    }

    public static class UpdateEntityRequest {
        private String name;
        private String description;
        private Integer schemaVersion;
        private Integer screenVersion;
        private String workflowId;
        private String authorizationModel;
        private String scope;
        private java.util.Map<String, Object> metadataJson;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getSchemaVersion() { return schemaVersion; }
        public void setSchemaVersion(Integer schemaVersion) { this.schemaVersion = schemaVersion; }
        public Integer getScreenVersion() { return screenVersion; }
        public void setScreenVersion(Integer screenVersion) { this.screenVersion = screenVersion; }
        public String getWorkflowId() { return workflowId; }
        public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
        public String getAuthorizationModel() { return authorizationModel; }
        public void setAuthorizationModel(String authorizationModel) { this.authorizationModel = authorizationModel; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
        public java.util.Map<String, Object> getMetadataJson() { return metadataJson; }
        public void setMetadataJson(java.util.Map<String, Object> metadataJson) { this.metadataJson = metadataJson; }
    }
}
