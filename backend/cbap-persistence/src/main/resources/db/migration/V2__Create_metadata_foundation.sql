-- CBAP OSS - Metadata Foundation Tables Migration
-- Creates base tables for metadata storage (entities, properties)
-- Additional metadata tables (workflows, measures, documents) will be added in later phases

-- ============================================================================
-- ENTITY METADATA TABLE
-- ============================================================================
CREATE TABLE cbap_metadata_entities (
    entity_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    schema_version INTEGER NOT NULL DEFAULT 1,
    screen_version INTEGER NOT NULL DEFAULT 1,
    workflow_id VARCHAR(255),
    authorization_model VARCHAR(50),
    scope VARCHAR(20) CHECK (scope IN ('LOCAL', 'GLOBAL', 'SHARED')),
    metadata_json JSONB,
    tenant_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT fk_metadata_entities_created_by FOREIGN KEY (created_by) REFERENCES cbap_users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_metadata_entities_name ON cbap_metadata_entities(name);
CREATE INDEX idx_metadata_entities_tenant_id ON cbap_metadata_entities(tenant_id);
CREATE INDEX idx_metadata_entities_workflow_id ON cbap_metadata_entities(workflow_id);
CREATE INDEX idx_metadata_entities_scope ON cbap_metadata_entities(scope);
CREATE INDEX idx_metadata_entities_metadata_json ON cbap_metadata_entities USING GIN (metadata_json);

-- ============================================================================
-- PROPERTY METADATA TABLE
-- ============================================================================
CREATE TABLE cbap_metadata_properties (
    property_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_id VARCHAR(255) NOT NULL,
    property_name VARCHAR(255) NOT NULL,
    property_type VARCHAR(50) NOT NULL CHECK (property_type IN (
        'string', 'number', 'date', 'boolean', 
        'singleSelect', 'multiSelect', 'reference', 'calculated'
    )),
    label VARCHAR(255),
    label_key VARCHAR(255),
    required BOOLEAN NOT NULL DEFAULT FALSE,
    read_only BOOLEAN NOT NULL DEFAULT FALSE,
    denormalize BOOLEAN NOT NULL DEFAULT FALSE,
    reference_entity_id VARCHAR(255),
    calculation_expression TEXT,
    metadata_json JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_metadata_properties_entity FOREIGN KEY (entity_id) REFERENCES cbap_metadata_entities(entity_id) ON DELETE CASCADE,
    CONSTRAINT fk_metadata_properties_reference FOREIGN KEY (reference_entity_id) REFERENCES cbap_metadata_entities(entity_id) ON DELETE SET NULL,
    CONSTRAINT uq_metadata_properties_entity_name UNIQUE (entity_id, property_name)
);

CREATE INDEX idx_metadata_properties_entity_id ON cbap_metadata_properties(entity_id);
CREATE INDEX idx_metadata_properties_property_name ON cbap_metadata_properties(property_name);
CREATE INDEX idx_metadata_properties_reference_entity_id ON cbap_metadata_properties(reference_entity_id);
CREATE INDEX idx_metadata_properties_denormalize ON cbap_metadata_properties(denormalize) WHERE denormalize = TRUE;
CREATE INDEX idx_metadata_properties_metadata_json ON cbap_metadata_properties USING GIN (metadata_json);

-- ============================================================================
-- TRIGGER: Update updated_at for metadata tables
-- ============================================================================
CREATE TRIGGER update_metadata_entities_updated_at BEFORE UPDATE ON cbap_metadata_entities
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_metadata_properties_updated_at BEFORE UPDATE ON cbap_metadata_properties
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
