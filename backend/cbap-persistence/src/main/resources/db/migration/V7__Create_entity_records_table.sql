-- CBAP OSS - Entity Records Storage Migration
-- Creates table for storing entity records with JSONB data

-- ============================================================================
-- ENTITY RECORDS TABLE
-- ============================================================================
CREATE TABLE cbap_entity_records (
    record_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_id VARCHAR(255) NOT NULL,
    data_json JSONB NOT NULL, -- Flexible JSONB storage for entity data
    schema_version INTEGER NOT NULL DEFAULT 1, -- Schema version at time of creation
    state VARCHAR(100), -- Current workflow state (if workflow is attached)
    tenant_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE, -- Soft delete support
    CONSTRAINT fk_entity_records_entity FOREIGN KEY (entity_id) REFERENCES cbap_metadata_entities(entity_id) ON DELETE RESTRICT,
    CONSTRAINT fk_entity_records_created_by FOREIGN KEY (created_by) REFERENCES cbap_users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_entity_records_updated_by FOREIGN KEY (updated_by) REFERENCES cbap_users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_entity_records_entity_id ON cbap_entity_records(entity_id);
CREATE INDEX idx_entity_records_tenant_id ON cbap_entity_records(tenant_id);
CREATE INDEX idx_entity_records_schema_version ON cbap_entity_records(entity_id, schema_version);
CREATE INDEX idx_entity_records_state ON cbap_entity_records(entity_id, state) WHERE state IS NOT NULL;
CREATE INDEX idx_entity_records_created_at ON cbap_entity_records(entity_id, created_at DESC);
CREATE INDEX idx_entity_records_deleted_at ON cbap_entity_records(deleted_at) WHERE deleted_at IS NULL; -- Only index non-deleted records
CREATE INDEX idx_entity_records_data_json ON cbap_entity_records USING GIN (data_json); -- GIN index for JSONB queries

-- ============================================================================
-- TRIGGER: Update updated_at for entity records
-- ============================================================================
CREATE TRIGGER update_entity_records_updated_at BEFORE UPDATE ON cbap_entity_records
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
