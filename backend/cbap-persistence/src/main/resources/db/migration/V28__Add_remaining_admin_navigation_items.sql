-- CBAP OSS - Add Remaining Admin Navigation Items
-- Adds all remaining admin pages to the Administration navigation section

-- Workflow Definitions (under Administration, Admin role only)
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

-- Measure Definitions (under Administration, Admin role only)
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
    '00000000-0000-0000-0000-000000000307',
    '00000000-0000-0000-0000-000000000302', -- Parent: Administration
    'Measure Definitions',
    'navigation.measureDefinitions',
    'Functions',
    '/admin/measures',
    5,
    'Administration',
    'Admin',
    'SYSTEM_MEASURE_DEFINITION',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (navigation_id) DO NOTHING;

-- System Settings (under Administration, Admin role only)
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
    '00000000-0000-0000-0000-000000000308',
    '00000000-0000-0000-0000-000000000302', -- Parent: Administration
    'System Settings',
    'navigation.systemSettings',
    'Settings',
    '/admin/system/settings',
    6,
    'Administration',
    'Admin',
    'SYSTEM_SETTINGS_MANAGEMENT',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (navigation_id) DO NOTHING;

-- Org Topology (under Administration, Admin role only)
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
    '00000000-0000-0000-0000-000000000309',
    '00000000-0000-0000-0000-000000000302', -- Parent: Administration
    'Org Topology',
    'navigation.orgTopology',
    'Business',
    '/admin/org-topology',
    7,
    'Administration',
    'Admin',
    'SYSTEM_ORG_TOPOLOGY_MANAGEMENT',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (navigation_id) DO NOTHING;

-- Licensing Status (under Administration, Admin role only)
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
    '00000000-0000-0000-0000-000000000310',
    '00000000-0000-0000-0000-000000000302', -- Parent: Administration
    'Licensing Status',
    'navigation.licensingStatus',
    'VerifiedUser',
    '/admin/system/licensing',
    8,
    'Administration',
    'Admin',
    'SYSTEM_LICENSING_VIEW',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (navigation_id) DO NOTHING;
