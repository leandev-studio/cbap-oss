-- CBAP OSS - Create Task Workflow
-- Creates a workflow for Task entity to support state transitions (OPEN -> IN_PROGRESS -> DONE)

-- ============================================================================
-- TASK WORKFLOW DEFINITION
-- ============================================================================

-- Create TaskWorkflow
INSERT INTO cbap_metadata_workflows (
    workflow_id,
    name,
    description,
    initial_state,
    metadata_json,
    tenant_id,
    created_at,
    updated_at
) VALUES (
    'TaskWorkflow',
    'Task Workflow',
    'Workflow for managing task status transitions',
    'OPEN',
    '{}'::jsonb,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (workflow_id) DO NOTHING;

-- ============================================================================
-- TASK WORKFLOW STATES
-- ============================================================================

-- State: OPEN
INSERT INTO cbap_metadata_workflow_states (
    state_id,
    workflow_id,
    state_name,
    label,
    label_key,
    description,
    is_initial,
    is_final,
    metadata_json,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000401',
    'TaskWorkflow',
    'OPEN',
    'Open',
    'task.state.open',
    'Task is open and ready to be worked on',
    TRUE,
    FALSE,
    '{}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (state_id) DO NOTHING;

-- State: IN_PROGRESS
INSERT INTO cbap_metadata_workflow_states (
    state_id,
    workflow_id,
    state_name,
    label,
    label_key,
    description,
    is_initial,
    is_final,
    metadata_json,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000402',
    'TaskWorkflow',
    'IN_PROGRESS',
    'In Progress',
    'task.state.in_progress',
    'Task is currently being worked on',
    FALSE,
    FALSE,
    '{}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (state_id) DO NOTHING;

-- State: DONE
INSERT INTO cbap_metadata_workflow_states (
    state_id,
    workflow_id,
    state_name,
    label,
    label_key,
    description,
    is_initial,
    is_final,
    metadata_json,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000403',
    'TaskWorkflow',
    'DONE',
    'Done',
    'task.state.done',
    'Task has been completed',
    FALSE,
    TRUE,
    '{}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (state_id) DO NOTHING;

-- State: CANCELLED
INSERT INTO cbap_metadata_workflow_states (
    state_id,
    workflow_id,
    state_name,
    label,
    label_key,
    description,
    is_initial,
    is_final,
    metadata_json,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000404',
    'TaskWorkflow',
    'CANCELLED',
    'Cancelled',
    'task.state.cancelled',
    'Task has been cancelled',
    FALSE,
    TRUE,
    '{}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (state_id) DO NOTHING;

-- ============================================================================
-- TASK WORKFLOW TRANSITIONS
-- ============================================================================

-- Transition: OPEN -> IN_PROGRESS (Start Work)
INSERT INTO cbap_metadata_workflow_transitions (
    transition_id,
    workflow_id,
    from_state,
    to_state,
    action_label,
    label_key,
    description,
    conditions_json,
    allowed_roles,
    pre_transition_rules,
    metadata_json,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000501',
    'TaskWorkflow',
    'OPEN',
    'IN_PROGRESS',
    'Start Work',
    'task.transition.start_work',
    'Mark task as in progress',
    '{}'::jsonb,
    NULL, -- All users can start work on their assigned tasks
    '{}'::jsonb,
    '{}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (transition_id) DO NOTHING;

-- Transition: IN_PROGRESS -> DONE (Complete)
INSERT INTO cbap_metadata_workflow_transitions (
    transition_id,
    workflow_id,
    from_state,
    to_state,
    action_label,
    label_key,
    description,
    conditions_json,
    allowed_roles,
    pre_transition_rules,
    metadata_json,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000502',
    'TaskWorkflow',
    'IN_PROGRESS',
    'DONE',
    'Complete',
    'task.transition.complete',
    'Mark task as done',
    '{}'::jsonb,
    NULL, -- All users can complete their assigned tasks
    '{}'::jsonb,
    '{}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (transition_id) DO NOTHING;

-- Transition: OPEN -> DONE (Complete Directly)
INSERT INTO cbap_metadata_workflow_transitions (
    transition_id,
    workflow_id,
    from_state,
    to_state,
    action_label,
    label_key,
    description,
    conditions_json,
    allowed_roles,
    pre_transition_rules,
    metadata_json,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000503',
    'TaskWorkflow',
    'OPEN',
    'DONE',
    'Complete',
    'task.transition.complete',
    'Mark task as done without going through in progress',
    '{}'::jsonb,
    NULL,
    '{}'::jsonb,
    '{}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (transition_id) DO NOTHING;

-- Transition: OPEN -> CANCELLED (Cancel)
INSERT INTO cbap_metadata_workflow_transitions (
    transition_id,
    workflow_id,
    from_state,
    to_state,
    action_label,
    label_key,
    description,
    conditions_json,
    allowed_roles,
    pre_transition_rules,
    metadata_json,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000504',
    'TaskWorkflow',
    'OPEN',
    'CANCELLED',
    'Cancel',
    'task.transition.cancel',
    'Cancel the task',
    '{}'::jsonb,
    NULL,
    '{}'::jsonb,
    '{}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (transition_id) DO NOTHING;

-- Transition: IN_PROGRESS -> CANCELLED (Cancel)
INSERT INTO cbap_metadata_workflow_transitions (
    transition_id,
    workflow_id,
    from_state,
    to_state,
    action_label,
    label_key,
    description,
    conditions_json,
    allowed_roles,
    pre_transition_rules,
    metadata_json,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000505',
    'TaskWorkflow',
    'IN_PROGRESS',
    'CANCELLED',
    'Cancel',
    'task.transition.cancel',
    'Cancel the task',
    '{}'::jsonb,
    NULL,
    '{}'::jsonb,
    '{}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (transition_id) DO NOTHING;

-- ============================================================================
-- CREATE TASK ENTITY DEFINITION
-- ============================================================================

-- Create Task entity definition
INSERT INTO cbap_metadata_entities (
    entity_id,
    name,
    description,
    workflow_id,
    authorization_model,
    scope,
    schema_version,
    screen_version,
    metadata_json,
    tenant_id,
    created_at,
    updated_at
) VALUES (
    'Task',
    'Task',
    'Task entity for workflow task management',
    'TaskWorkflow',
    'role_based',
    'LOCAL',
    1,
    1,
    '{"displayField": "title", "searchDisplay": "title"}'::jsonb,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (entity_id) DO UPDATE
SET 
    workflow_id = 'TaskWorkflow',
    metadata_json = COALESCE(cbap_metadata_entities.metadata_json, '{}'::jsonb) || '{"displayField": "title", "searchDisplay": "title"}'::jsonb,
    updated_at = CURRENT_TIMESTAMP;
