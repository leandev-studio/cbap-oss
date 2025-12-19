-- CBAP OSS - Seed Initial Data
-- Inserts default roles, permissions, and admin user for initial setup

-- ============================================================================
-- DEFAULT ROLES
-- ============================================================================
INSERT INTO cbap_roles (role_id, role_name, description, tenant_id) VALUES
    ('00000000-0000-0000-0000-000000000001', 'Admin', 'Full system access and administration', NULL),
    ('00000000-0000-0000-0000-000000000002', 'User', 'Basic user access to view and create records', NULL),
    ('00000000-0000-0000-0000-000000000003', 'Designer', 'Can create and edit entity definitions, workflows, and measures', NULL),
    ('00000000-0000-0000-0000-000000000004', 'Approver', 'Can approve workflow transitions and make decisions', NULL)
ON CONFLICT (role_name) DO NOTHING;

-- ============================================================================
-- DEFAULT PERMISSIONS
-- ============================================================================

-- System Permissions
INSERT INTO cbap_permissions (permission_id, permission_name, resource_type, action, description) VALUES
    ('10000000-0000-0000-0000-000000000001', 'SYSTEM_USER_MANAGEMENT', 'SYSTEM', 'MANAGE', 'Manage users'),
    ('10000000-0000-0000-0000-000000000002', 'SYSTEM_ROLE_MANAGEMENT', 'SYSTEM', 'MANAGE', 'Manage roles and permissions'),
    ('10000000-0000-0000-0000-000000000003', 'SYSTEM_ENTITY_DEFINITION', 'SYSTEM', 'MANAGE', 'Create and edit entity definitions'),
    ('10000000-0000-0000-0000-000000000004', 'SYSTEM_WORKFLOW_DEFINITION', 'SYSTEM', 'MANAGE', 'Create and edit workflow definitions'),
    ('10000000-0000-0000-0000-000000000005', 'SYSTEM_MEASURE_DEFINITION', 'SYSTEM', 'MANAGE', 'Create and edit measure definitions'),
    ('10000000-0000-0000-0000-000000000006', 'SYSTEM_ORG_TOPOLOGY', 'SYSTEM', 'MANAGE', 'Manage organization topology'),
    ('10000000-0000-0000-0000-000000000007', 'SYSTEM_LICENSING', 'SYSTEM', 'VIEW', 'View licensing status'),
    ('10000000-0000-0000-0000-000000000008', 'SYSTEM_TELEMETRY', 'SYSTEM', 'VIEW', 'View telemetry and diagnostics')
ON CONFLICT (permission_name) DO NOTHING;

-- Entity Permissions (generic - apply to all entities)
INSERT INTO cbap_permissions (permission_id, permission_name, resource_type, action, description) VALUES
    ('20000000-0000-0000-0000-000000000001', 'ENTITY_CREATE', 'ENTITY', 'CREATE', 'Create entity records'),
    ('20000000-0000-0000-0000-000000000002', 'ENTITY_READ', 'ENTITY', 'READ', 'Read entity records'),
    ('20000000-0000-0000-0000-000000000003', 'ENTITY_UPDATE', 'ENTITY', 'UPDATE', 'Update entity records'),
    ('20000000-0000-0000-0000-000000000004', 'ENTITY_DELETE', 'ENTITY', 'DELETE', 'Delete entity records'),
    ('20000000-0000-0000-0000-000000000005', 'ENTITY_PROPERTY_READ', 'ENTITY', 'READ', 'Read entity property values'),
    ('20000000-0000-0000-0000-000000000006', 'ENTITY_PROPERTY_WRITE', 'ENTITY', 'WRITE', 'Write entity property values')
ON CONFLICT (permission_name) DO NOTHING;

-- Workflow Permissions
INSERT INTO cbap_permissions (permission_id, permission_name, resource_type, action, description) VALUES
    ('30000000-0000-0000-0000-000000000001', 'WORKFLOW_EXECUTE', 'WORKFLOW', 'EXECUTE', 'Execute workflow transitions'),
    ('30000000-0000-0000-0000-000000000002', 'WORKFLOW_APPROVE', 'WORKFLOW', 'APPROVE', 'Approve workflow transitions'),
    ('30000000-0000-0000-0000-000000000003', 'WORKFLOW_REJECT', 'WORKFLOW', 'REJECT', 'Reject workflow transitions')
ON CONFLICT (permission_name) DO NOTHING;

-- ============================================================================
-- ROLE-PERMISSION ASSIGNMENTS
-- ============================================================================

-- Admin Role: All permissions
INSERT INTO cbap_role_permissions (role_id, permission_id)
SELECT 
    '00000000-0000-0000-0000-000000000001'::UUID,
    permission_id
FROM cbap_permissions
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- User Role: Basic entity access
INSERT INTO cbap_role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001'), -- ENTITY_CREATE
    ('00000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002'), -- ENTITY_READ
    ('00000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000003'), -- ENTITY_UPDATE
    ('00000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000005'), -- ENTITY_PROPERTY_READ
    ('00000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000006'), -- ENTITY_PROPERTY_WRITE
    ('00000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001')  -- WORKFLOW_EXECUTE
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Designer Role: Entity and workflow definition, basic entity access
INSERT INTO cbap_role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003'), -- SYSTEM_ENTITY_DEFINITION
    ('00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000004'), -- SYSTEM_WORKFLOW_DEFINITION
    ('00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000005'), -- SYSTEM_MEASURE_DEFINITION
    ('00000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001'), -- ENTITY_CREATE
    ('00000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000002'), -- ENTITY_READ
    ('00000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003'), -- ENTITY_UPDATE
    ('00000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000005'), -- ENTITY_PROPERTY_READ
    ('00000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000006'), -- ENTITY_PROPERTY_WRITE
    ('00000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001')  -- WORKFLOW_EXECUTE
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Approver Role: Entity read and workflow approval
INSERT INTO cbap_role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002'), -- ENTITY_READ
    ('00000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000005'), -- ENTITY_PROPERTY_READ
    ('00000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000001'), -- WORKFLOW_EXECUTE
    ('00000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000002'), -- WORKFLOW_APPROVE
    ('00000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000003')  -- WORKFLOW_REJECT
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ============================================================================
-- DEFAULT ADMIN USER
-- ============================================================================
-- Password: 'admin123' (BCrypt hash with cost 12, matching PasswordHashingService)
-- This is a default password - should be changed on first login
-- In production, use environment variable or secure initialization
-- 
-- To generate a new hash, run:
-- mvn exec:java -Dexec.mainClass="com.cbap.security.util.PasswordHashGenerator" -pl cbap-security
INSERT INTO cbap_users (
    user_id,
    username,
    password_hash,
    email,
    status,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000100',
    'admin',
    '$2a$12$/9LuepbRMb1MRKjiy64ZSujYI.0Ie/alpfsZWApVF2KcpFNCMSeYm', -- BCrypt hash of 'admin123' (cost 12)
    'admin@cbap.local',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

-- Assign Admin role to admin user
INSERT INTO cbap_user_roles (user_id, role_id, assigned_at) VALUES
    ('00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP)
ON CONFLICT (user_id, role_id) DO NOTHING;

-- ============================================================================
-- DEFAULT HQ ORG UNIT
-- ============================================================================
-- Create a default tenant and HQ org unit for initial setup
-- In production, this would be created during tenant onboarding
INSERT INTO cbap_org_units (
    org_unit_id,
    tenant_id,
    org_unit_type,
    name,
    parent_org_unit_id,
    timezone,
    status,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000200',
    '00000000-0000-0000-0000-000000000000', -- Default tenant ID
    'HQ',
    'Headquarters',
    NULL,
    'UTC',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT DO NOTHING;
