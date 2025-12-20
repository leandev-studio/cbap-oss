-- CBAP OSS - Add Entity Definitions to Administration
-- Adds Entity Definitions management navigation item under Administration

-- Entity Definitions Management (under Administration, Admin role only)
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
    '00000000-0000-0000-0000-000000000305',
    '00000000-0000-0000-0000-000000000302', -- Parent: Administration
    'Entity Definitions',
    'navigation.entityDefinitions',
    'Schema',
    '/admin/entity-definitions',
    3,
    'Administration',
    'Admin',
    'SYSTEM_ENTITY_DEFINITION', -- Requires permission
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (navigation_id) DO NOTHING;
