package com.cbap.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Property definition metadata.
 * 
 * Represents a property/field definition for an entity.
 */
@Entity
@Table(name = "cbap_metadata_properties")
public class PropertyDefinition {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    @Column(name = "property_id")
    private UUID propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id", nullable = false)
    private EntityDefinition entity;

    @Column(name = "property_name", nullable = false, length = 255)
    private String propertyName;

    @Column(name = "property_type", nullable = false, length = 50)
    private String propertyType;

    @Column(name = "label", length = 255)
    private String label;

    @Column(name = "label_key", length = 255)
    private String labelKey;

    @Column(name = "required", nullable = false)
    private Boolean required = false;

    @Column(name = "read_only", nullable = false)
    private Boolean readOnly = false;

    @Column(name = "denormalize", nullable = false)
    private Boolean denormalize = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_entity_id")
    private EntityDefinition referenceEntity;

    @Column(name = "calculation_expression", columnDefinition = "TEXT")
    private String calculationExpression;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", columnDefinition = "jsonb")
    private Map<String, Object> metadataJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
        if (required == null) {
            required = false;
        }
        if (readOnly == null) {
            readOnly = false;
        }
        if (denormalize == null) {
            denormalize = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public UUID getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(UUID propertyId) {
        this.propertyId = propertyId;
    }

    public EntityDefinition getEntity() {
        return entity;
    }

    public void setEntity(EntityDefinition entity) {
        this.entity = entity;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Boolean getDenormalize() {
        return denormalize;
    }

    public void setDenormalize(Boolean denormalize) {
        this.denormalize = denormalize;
    }

    public EntityDefinition getReferenceEntity() {
        return referenceEntity;
    }

    public void setReferenceEntity(EntityDefinition referenceEntity) {
        this.referenceEntity = referenceEntity;
    }

    public String getCalculationExpression() {
        return calculationExpression;
    }

    public void setCalculationExpression(String calculationExpression) {
        this.calculationExpression = calculationExpression;
    }

    public Map<String, Object> getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(Map<String, Object> metadataJson) {
        this.metadataJson = metadataJson;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
