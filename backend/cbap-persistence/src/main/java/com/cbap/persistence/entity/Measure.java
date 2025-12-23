package com.cbap.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Measure metadata.
 * 
 * Represents a declarative, read-only function defined via metadata.
 * Measures encapsulate reusable business calculations.
 */
@Entity
@Table(name = "cbap_metadata_measures")
public class Measure {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    @Column(name = "measure_id")
    private UUID measureId;

    @Column(name = "measure_identifier", nullable = false, length = 255)
    private String measureIdentifier;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parameters_json", columnDefinition = "jsonb")
    private List<Map<String, Object>> parametersJson;

    @Column(name = "return_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ReturnType returnType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "depends_on_json", columnDefinition = "jsonb")
    private List<Map<String, Object>> dependsOnJson;

    @Column(name = "definition_type", nullable = false, length = 50)
    private String definitionType = "expression";

    @Column(name = "expression", nullable = false, columnDefinition = "TEXT")
    private String expression;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", columnDefinition = "jsonb")
    private Map<String, Object> metadataJson;

    @Column(name = "schema_version", nullable = false)
    private Integer schemaVersion = 1;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // Enums
    public enum ReturnType {
        number,
        string,
        bool,  // Note: 'boolean' is a reserved keyword, using 'bool' instead
        date,
        reference
    }

    // Getters and Setters
    public UUID getMeasureId() {
        return measureId;
    }

    public void setMeasureId(UUID measureId) {
        this.measureId = measureId;
    }

    public String getMeasureIdentifier() {
        return measureIdentifier;
    }

    public void setMeasureIdentifier(String measureIdentifier) {
        this.measureIdentifier = measureIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<Map<String, Object>> getParametersJson() {
        return parametersJson;
    }

    public void setParametersJson(List<Map<String, Object>> parametersJson) {
        this.parametersJson = parametersJson;
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public void setReturnType(ReturnType returnType) {
        this.returnType = returnType;
    }

    public List<Map<String, Object>> getDependsOnJson() {
        return dependsOnJson;
    }

    public void setDependsOnJson(List<Map<String, Object>> dependsOnJson) {
        this.dependsOnJson = dependsOnJson;
    }

    public String getDefinitionType() {
        return definitionType;
    }

    public void setDefinitionType(String definitionType) {
        this.definitionType = definitionType;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Map<String, Object> getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(Map<String, Object> metadataJson) {
        this.metadataJson = metadataJson;
    }

    public Integer getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(Integer schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}
