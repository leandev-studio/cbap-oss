-- CBAP OSS - Workflow Metadata Tables Migration
-- Creates tables for workflow definitions, states, and transitions

-- ============================================================================
-- WORKFLOW DEFINITIONS TABLE
-- ============================================================================
CREATE TABLE cbap_metadata_workflows (
    workflow_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    initial_state VARCHAR(100) NOT NULL,
    metadata_json JSONB,
    tenant_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT fk_metadata_workflows_created_by FOREIGN KEY (created_by) REFERENCES cbap_users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_metadata_workflows_name ON cbap_metadata_workflows(name);
CREATE INDEX idx_metadata_workflows_tenant_id ON cbap_metadata_workflows(tenant_id);
CREATE INDEX idx_metadata_workflows_metadata_json ON cbap_metadata_workflows USING GIN (metadata_json);

-- ============================================================================
-- WORKFLOW STATES TABLE
-- ============================================================================
CREATE TABLE cbap_metadata_workflow_states (
    state_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workflow_id VARCHAR(255) NOT NULL,
    state_name VARCHAR(100) NOT NULL,
    label VARCHAR(255),
    label_key VARCHAR(255),
    description TEXT,
    is_initial BOOLEAN NOT NULL DEFAULT FALSE,
    is_final BOOLEAN NOT NULL DEFAULT FALSE,
    metadata_json JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workflow_states_workflow FOREIGN KEY (workflow_id) REFERENCES cbap_metadata_workflows(workflow_id) ON DELETE CASCADE,
    CONSTRAINT uq_workflow_states_workflow_name UNIQUE (workflow_id, state_name)
);

CREATE INDEX idx_workflow_states_workflow_id ON cbap_metadata_workflow_states(workflow_id);
CREATE INDEX idx_workflow_states_state_name ON cbap_metadata_workflow_states(state_name);
CREATE INDEX idx_workflow_states_metadata_json ON cbap_metadata_workflow_states USING GIN (metadata_json);

-- ============================================================================
-- WORKFLOW TRANSITIONS TABLE
-- ============================================================================
CREATE TABLE cbap_metadata_workflow_transitions (
    transition_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workflow_id VARCHAR(255) NOT NULL,
    from_state VARCHAR(100) NOT NULL,
    to_state VARCHAR(100) NOT NULL,
    action_label VARCHAR(255) NOT NULL,
    label_key VARCHAR(255),
    description TEXT,
    conditions_json JSONB, -- Conditions that must be met for transition
    allowed_roles JSONB, -- Array of role names allowed to execute this transition
    pre_transition_rules JSONB, -- Rules to evaluate before transition
    metadata_json JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workflow_transitions_workflow FOREIGN KEY (workflow_id) REFERENCES cbap_metadata_workflows(workflow_id) ON DELETE CASCADE,
    CONSTRAINT uq_workflow_transitions_workflow_from_to UNIQUE (workflow_id, from_state, to_state)
);

CREATE INDEX idx_workflow_transitions_workflow_id ON cbap_metadata_workflow_transitions(workflow_id);
CREATE INDEX idx_workflow_transitions_from_state ON cbap_metadata_workflow_transitions(from_state);
CREATE INDEX idx_workflow_transitions_to_state ON cbap_metadata_workflow_transitions(to_state);
CREATE INDEX idx_workflow_transitions_metadata_json ON cbap_metadata_workflow_transitions USING GIN (metadata_json);

-- ============================================================================
-- WORKFLOW AUDIT LOG TABLE
-- ============================================================================
CREATE TABLE cbap_workflow_audit_log (
    audit_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_id VARCHAR(255) NOT NULL,
    record_id UUID NOT NULL,
    workflow_id VARCHAR(255) NOT NULL,
    from_state VARCHAR(100),
    to_state VARCHAR(100) NOT NULL,
    transition_id UUID,
    transition_label VARCHAR(255),
    performed_by UUID NOT NULL,
    performed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comments TEXT,
    metadata_json JSONB,
    CONSTRAINT fk_workflow_audit_entity FOREIGN KEY (entity_id) REFERENCES cbap_metadata_entities(entity_id) ON DELETE CASCADE,
    CONSTRAINT fk_workflow_audit_record FOREIGN KEY (record_id) REFERENCES cbap_entity_records(record_id) ON DELETE CASCADE,
    CONSTRAINT fk_workflow_audit_workflow FOREIGN KEY (workflow_id) REFERENCES cbap_metadata_workflows(workflow_id) ON DELETE CASCADE,
    CONSTRAINT fk_workflow_audit_user FOREIGN KEY (performed_by) REFERENCES cbap_users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_workflow_audit_entity_record ON cbap_workflow_audit_log(entity_id, record_id);
CREATE INDEX idx_workflow_audit_workflow ON cbap_workflow_audit_log(workflow_id);
CREATE INDEX idx_workflow_audit_user ON cbap_workflow_audit_log(performed_by);
CREATE INDEX idx_workflow_audit_performed_at ON cbap_workflow_audit_log(performed_at);
CREATE INDEX idx_workflow_audit_metadata_json ON cbap_workflow_audit_log USING GIN (metadata_json);

-- ============================================================================
-- TRIGGER: Update updated_at for workflow metadata tables
-- ============================================================================
CREATE TRIGGER update_workflow_metadata_updated_at BEFORE UPDATE ON cbap_metadata_workflows
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workflow_states_updated_at BEFORE UPDATE ON cbap_metadata_workflow_states
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workflow_transitions_updated_at BEFORE UPDATE ON cbap_metadata_workflow_transitions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- FOREIGN KEY: Link entity definitions to workflows
-- ============================================================================
ALTER TABLE cbap_metadata_entities
    ADD CONSTRAINT fk_metadata_entities_workflow FOREIGN KEY (workflow_id) 
    REFERENCES cbap_metadata_workflows(workflow_id) ON DELETE SET NULL;
