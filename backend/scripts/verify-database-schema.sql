-- CBAP OSS - Database Schema Verification Script
-- Run this script after migrations to verify all tables, indexes, and constraints are created correctly

-- ============================================================================
-- Verify Tables Exist
-- ============================================================================
DO $$
DECLARE
    expected_tables TEXT[] := ARRAY[
        'cbap_users',
        'cbap_roles',
        'cbap_user_roles',
        'cbap_permissions',
        'cbap_role_permissions',
        'cbap_password_reset_tokens',
        'cbap_org_units',
        'cbap_metadata_entities',
        'cbap_metadata_properties'
    ];
    missing_tables TEXT[] := ARRAY[]::TEXT[];
    table_name TEXT;
BEGIN
    FOREACH table_name IN ARRAY expected_tables
    LOOP
        IF NOT EXISTS (
            SELECT FROM information_schema.tables 
            WHERE table_schema = 'public' 
            AND table_name = table_name
        ) THEN
            missing_tables := array_append(missing_tables, table_name);
        END IF;
    END LOOP;
    
    IF array_length(missing_tables, 1) > 0 THEN
        RAISE EXCEPTION 'Missing tables: %', array_to_string(missing_tables, ', ');
    ELSE
        RAISE NOTICE 'All expected tables exist';
    END IF;
END $$;

-- ============================================================================
-- Verify Indexes
-- ============================================================================
SELECT 
    'Index verification' AS check_type,
    COUNT(*) AS index_count
FROM pg_indexes
WHERE schemaname = 'public'
AND tablename LIKE 'cbap_%';

-- ============================================================================
-- Verify Foreign Keys
-- ============================================================================
SELECT 
    'Foreign key verification' AS check_type,
    COUNT(*) AS foreign_key_count
FROM information_schema.table_constraints
WHERE constraint_type = 'FOREIGN KEY'
AND table_schema = 'public'
AND table_name LIKE 'cbap_%';

-- ============================================================================
-- Verify Seed Data
-- ============================================================================
SELECT 
    'Roles count' AS check_type,
    COUNT(*) AS count
FROM cbap_roles
WHERE role_name IN ('Admin', 'User', 'Designer', 'Approver');

SELECT 
    'Permissions count' AS check_type,
    COUNT(*) AS count
FROM cbap_permissions;

SELECT 
    'Admin user exists' AS check_type,
    CASE WHEN COUNT(*) > 0 THEN 'YES' ELSE 'NO' END AS status
FROM cbap_users
WHERE username = 'admin';

SELECT 
    'Admin user has Admin role' AS check_type,
    CASE WHEN COUNT(*) > 0 THEN 'YES' ELSE 'NO' END AS status
FROM cbap_user_roles ur
JOIN cbap_users u ON ur.user_id = u.user_id
JOIN cbap_roles r ON ur.role_id = r.role_id
WHERE u.username = 'admin' AND r.role_name = 'Admin';

SELECT 
    'HQ OrgUnit exists' AS check_type,
    CASE WHEN COUNT(*) > 0 THEN 'YES' ELSE 'NO' END AS status
FROM cbap_org_units
WHERE org_unit_type = 'HQ';

-- ============================================================================
-- Table Row Counts
-- ============================================================================
SELECT 'cbap_users' AS table_name, COUNT(*) AS row_count FROM cbap_users
UNION ALL
SELECT 'cbap_roles', COUNT(*) FROM cbap_roles
UNION ALL
SELECT 'cbap_permissions', COUNT(*) FROM cbap_permissions
UNION ALL
SELECT 'cbap_role_permissions', COUNT(*) FROM cbap_role_permissions
UNION ALL
SELECT 'cbap_user_roles', COUNT(*) FROM cbap_user_roles
UNION ALL
SELECT 'cbap_org_units', COUNT(*) FROM cbap_org_units;

-- ============================================================================
-- Verify UUID Extension
-- ============================================================================
SELECT 
    'UUID extension' AS check_type,
    CASE WHEN EXISTS (
        SELECT FROM pg_extension WHERE extname = 'uuid-ossp'
    ) THEN 'ENABLED' ELSE 'NOT ENABLED' END AS status;

-- ============================================================================
-- Verify Triggers
-- ============================================================================
SELECT 
    'Trigger verification' AS check_type,
    COUNT(*) AS trigger_count
FROM information_schema.triggers
WHERE trigger_schema = 'public'
AND trigger_name LIKE 'update_%_updated_at';

-- ============================================================================
-- Summary
-- ============================================================================
SELECT 
    'Schema verification complete' AS status,
    CURRENT_TIMESTAMP AS verified_at;
