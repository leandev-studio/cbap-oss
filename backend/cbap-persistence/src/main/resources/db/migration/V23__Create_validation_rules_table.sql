-- CBAP OSS - Create Validation Rules Table
-- Creates table for storing validation rules metadata

-- ============================================================================
-- VALIDATION RULES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS cbap_metadata_validation_rules (
    validation_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_id VARCHAR(255),
    property_name VARCHAR(255),
    rule_name VARCHAR(255) NOT NULL,
    description TEXT,
    scope VARCHAR(50) NOT NULL CHECK (scope IN ('FIELD', 'ENTITY', 'CROSS_ENTITY', 'WORKFLOW_TRANSITION')),
    rule_type VARCHAR(50) NOT NULL CHECK (rule_type IN ('REQUIRED', 'TYPE', 'RANGE', 'LENGTH', 'PATTERN', 'EXPRESSION', 'CUSTOM')),
    expression TEXT, -- CEL-v0 expression for rule evaluation
    error_message TEXT, -- User-friendly error message
    error_message_key VARCHAR(255), -- i18n key for error message
    trigger_events JSONB, -- When to trigger validation (e.g., ["CREATE", "UPDATE", "TRANSITION"])
    conditions_json JSONB, -- Conditional validation (if/then)
    metadata_json JSONB, -- Additional metadata (min, max, pattern, etc.)
    schema_version INTEGER NOT NULL DEFAULT 1,
    tenant_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT fk_validation_entity FOREIGN KEY (entity_id) REFERENCES cbap_metadata_entities(entity_id) ON DELETE CASCADE,
    CONSTRAINT fk_validation_property FOREIGN KEY (entity_id, property_name) REFERENCES cbap_metadata_properties(entity_id, property_name) ON DELETE CASCADE,
    CONSTRAINT fk_validation_created_by FOREIGN KEY (created_by) REFERENCES cbap_users(user_id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_validation_rules_entity ON cbap_metadata_validation_rules(entity_id);
CREATE INDEX idx_validation_rules_entity_property ON cbap_metadata_validation_rules(entity_id, property_name);
CREATE INDEX idx_validation_rules_scope ON cbap_metadata_validation_rules(scope);
CREATE INDEX idx_validation_rules_tenant ON cbap_metadata_validation_rules(tenant_id);

-- ============================================================================
-- NOTES
-- ============================================================================
-- scope: FIELD (property-level), ENTITY (entity-level), CROSS_ENTITY (cross-entity), WORKFLOW_TRANSITION (workflow-specific)
-- rule_type: REQUIRED, TYPE, RANGE, LENGTH, PATTERN, EXPRESSION (CEL-v0), CUSTOM
-- expression: CEL-v0 expression that evaluates to boolean (true = valid, false = invalid)
-- trigger_events: JSON array of events that trigger validation: ["CREATE", "UPDATE", "DELETE", "TRANSITION"]
-- conditions_json: Optional conditional logic, e.g., {"if": "status == 'APPROVED'", "then": "approver != null"}
-- metadata_json: Rule-specific metadata:
--   - For RANGE: {"min": 0, "max": 100}
--   - For LENGTH: {"minLength": 1, "maxLength": 255}
--   - For PATTERN: {"pattern": "^[A-Z0-9]+$"}
--   - For TYPE: {"expectedType": "number"}
-- ============================================================================
