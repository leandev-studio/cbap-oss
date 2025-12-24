-- CBAP OSS - Fix Workflow Definitions navigation item
-- Previous migration reused navigation_id '00000000-0000-0000-0000-000000000306'
-- which was already used by the Tasks menu entry (V19__Add_tasks_navigation_item).
-- As a result, the Workflow menu row was never inserted and V29 accidentally
-- changed the Tasks permission. This migration restores the Tasks item and
-- adds a new, unique Workflow Definitions item.

-- 1) Ensure the Tasks navigation item remains publicly visible (no special permission)
UPDATE cbap_navigation_items
SET
    label = 'Tasks',
    label_key = 'navigation.tasks',
    icon = 'Assignment',
    route_path = '/tasks',
    section = 'Main',
    display_order = 2,
    required_role = NULL,
    required_permission = NULL,
    visible = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE navigation_id = '00000000-0000-0000-0000-000000000306';

-- 2) Create a new Workflow Definitions navigation item with a new ID
INSERT INTO cbap_navigation_items (
    navigation_id,
    parent_navigation_id,
    label,
    label_key,
    icon,
    route_path,
    display_order,
    section,
    required_role,
    required_permission,
    visible,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000311',
    '00000000-0000-0000-0000-000000000302', -- Parent: Administration
    'Workflow Definitions',
    'navigation.workflowDefinitions',
    'AccountTree',
    '/admin/workflows',
    4,
    'Administration',
    'Admin',
    'SYSTEM_WORKFLOW_DEFINITION',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (navigation_id) DO NOTHING;

