-- CBAP OSS - Tasks Table Migration
-- Creates table for task management

-- ============================================================================
-- TASKS TABLE
-- ============================================================================
CREATE TABLE cbap_tasks (
    task_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_id VARCHAR(255) NOT NULL,
    record_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    assignee_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPEN', 'IN_PROGRESS', 'DONE', 'CANCELLED')) DEFAULT 'OPEN',
    priority VARCHAR(20) CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    due_date TIMESTAMP WITH TIME ZONE,
    workflow_state VARCHAR(100), -- Snapshot of workflow state when task was created
    transition_id UUID, -- Reference to workflow transition that created this task
    completed_at TIMESTAMP WITH TIME ZONE,
    completed_by UUID,
    decision VARCHAR(20) CHECK (decision IN ('APPROVED', 'REJECTED', 'REQUEST_CHANGES')),
    decision_comments TEXT,
    metadata_json JSONB,
    tenant_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT fk_tasks_entity FOREIGN KEY (entity_id) REFERENCES cbap_metadata_entities(entity_id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_record FOREIGN KEY (record_id) REFERENCES cbap_entity_records(record_id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_assignee FOREIGN KEY (assignee_id) REFERENCES cbap_users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_completed_by FOREIGN KEY (completed_by) REFERENCES cbap_users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_tasks_created_by FOREIGN KEY (created_by) REFERENCES cbap_users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_tasks_transition FOREIGN KEY (transition_id) REFERENCES cbap_metadata_workflow_transitions(transition_id) ON DELETE SET NULL
);

CREATE INDEX idx_tasks_entity_record ON cbap_tasks(entity_id, record_id);
CREATE INDEX idx_tasks_assignee ON cbap_tasks(assignee_id);
CREATE INDEX idx_tasks_status ON cbap_tasks(status);
CREATE INDEX idx_tasks_due_date ON cbap_tasks(due_date);
CREATE INDEX idx_tasks_created_at ON cbap_tasks(created_at);
CREATE INDEX idx_tasks_metadata_json ON cbap_tasks USING GIN (metadata_json);

-- ============================================================================
-- TRIGGER: Update updated_at for tasks table
-- ============================================================================
CREATE TRIGGER update_tasks_updated_at BEFORE UPDATE ON cbap_tasks
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
