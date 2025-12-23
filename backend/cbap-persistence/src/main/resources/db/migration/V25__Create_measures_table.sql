-- CBAP OSS - Create Measures Table
-- Creates table for storing measure definitions (declarative functions)

-- ============================================================================
-- MEASURES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS cbap_metadata_measures (
    measure_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    measure_identifier VARCHAR(255) NOT NULL, -- e.g., "budget.available"
    name VARCHAR(255) NOT NULL,
    description TEXT,
    version INTEGER NOT NULL DEFAULT 1,
    parameters_json JSONB, -- Array of parameter definitions: [{"name": "department", "type": "reference:Department", "default": null}]
    return_type VARCHAR(50) NOT NULL CHECK (return_type IN ('number', 'string', 'bool', 'date', 'reference')),
    depends_on_json JSONB, -- Array of dependencies: [{"entity": "Budget", "fields": ["allocatedAmount"], "dimensions": ["department", "period"]}]
    definition_type VARCHAR(50) NOT NULL DEFAULT 'expression' CHECK (definition_type IN ('expression')),
    expression TEXT NOT NULL, -- CEL-v0 expression
    metadata_json JSONB, -- Additional metadata
    schema_version INTEGER NOT NULL DEFAULT 1,
    tenant_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT fk_measure_created_by FOREIGN KEY (created_by) REFERENCES cbap_users(user_id) ON DELETE SET NULL,
    CONSTRAINT uk_measure_identifier_version UNIQUE (measure_identifier, version) -- Unique constraint on identifier + version
);

-- Indexes
CREATE INDEX idx_measures_identifier ON cbap_metadata_measures(measure_identifier);
CREATE INDEX idx_measures_version ON cbap_metadata_measures(measure_identifier, version);
CREATE INDEX idx_measures_tenant ON cbap_metadata_measures(tenant_id);

-- ============================================================================
-- NOTES
-- ============================================================================
-- measure_identifier: Unique identifier for the measure (e.g., "budget.available")
-- version: Version number for the measure (allows versioning)
-- parameters_json: JSON array of parameter definitions:
--   [{"name": "department", "type": "reference:Department", "default": null}]
-- return_type: The type of value returned by the measure
-- depends_on_json: JSON array of dependencies:
--   [{"entity": "Budget", "fields": ["allocatedAmount", "department"], "dimensions": ["department", "period"]}]
-- definition_type: Currently only "expression" is supported
-- expression: CEL-v0 expression that defines the measure calculation
-- metadata_json: Additional metadata (e.g., caching hints, documentation)
-- ============================================================================
