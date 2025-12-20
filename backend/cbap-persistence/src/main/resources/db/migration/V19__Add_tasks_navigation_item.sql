-- CBAP OSS - Add Tasks Navigation Item
-- Adds Tasks navigation item after Dashboard

-- ============================================================================
-- ADD TASKS NAVIGATION ITEM
-- ============================================================================

-- Tasks (accessible to all authenticated users, positioned after Dashboard)
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
    '00000000-0000-0000-0000-000000000306',
    NULL,
    'Tasks',
    'navigation.tasks',
    'Assignment',
    '/tasks',
    2,
    'Main',
    NULL, -- Accessible to all authenticated users
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (navigation_id) DO NOTHING;

-- Update Entities display_order to 3 (to come after Tasks)
UPDATE cbap_navigation_items
SET
    display_order = 3,
    updated_at = CURRENT_TIMESTAMP
WHERE navigation_id = '00000000-0000-0000-0000-000000000301';
