-- CBAP OSS - Seed Navigation Metadata
-- Inserts default navigation items for initial setup

-- ============================================================================
-- DEFAULT NAVIGATION ITEMS
-- ============================================================================

-- Dashboard (accessible to all authenticated users)
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
    '00000000-0000-0000-0000-000000000300',
    NULL,
    'Dashboard',
    'navigation.dashboard',
    'Dashboard',
    '/',
    1,
    'Main',
    NULL, -- Accessible to all authenticated users
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT DO NOTHING;

-- Entities Section (accessible to users with ENTITY_READ permission)
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
    '00000000-0000-0000-0000-000000000301',
    NULL,
    'Entities',
    'navigation.entities',
    'TableChart',
    '/entities',
    2,
    'Main',
    NULL,
    'ENTITY_READ', -- Requires ENTITY_READ permission
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT DO NOTHING;

-- Administration Section (accessible to Admin role only)
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
    '00000000-0000-0000-0000-000000000302',
    NULL,
    'Administration',
    'navigation.administration',
    'Settings',
    '/admin',
    10,
    'Administration',
    'Admin', -- Requires Admin role
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT DO NOTHING;

-- User Management (under Administration, Admin role only)
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
    '00000000-0000-0000-0000-000000000303',
    '00000000-0000-0000-0000-000000000302', -- Parent: Administration
    'Users',
    'navigation.users',
    'People',
    '/admin/users',
    1,
    'Administration',
    'Admin',
    'SYSTEM_USER_MANAGEMENT', -- Also requires permission
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT DO NOTHING;

-- Roles & Permissions (under Administration, Admin role only)
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
    '00000000-0000-0000-0000-000000000304',
    '00000000-0000-0000-0000-000000000302', -- Parent: Administration
    'Roles & Permissions',
    'navigation.roles',
    'Security',
    '/admin/roles',
    2,
    'Administration',
    'Admin',
    'SYSTEM_ROLE_MANAGEMENT', -- Also requires permission
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT DO NOTHING;
