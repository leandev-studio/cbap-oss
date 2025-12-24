-- CBAP OSS - Align Admin Navigation Permissions
-- Ensures newly added Administration navigation items use existing permission names
-- so that seeded roles (especially Admin) can see all admin pages.

-- Workflow Definitions navigation item should require SYSTEM_WORKFLOW_DEFINITION
UPDATE cbap_navigation_items
SET required_permission = 'SYSTEM_WORKFLOW_DEFINITION'
WHERE navigation_id = '00000000-0000-0000-0000-000000000306';

-- Measure Definitions navigation item should require SYSTEM_MEASURE_DEFINITION
UPDATE cbap_navigation_items
SET required_permission = 'SYSTEM_MEASURE_DEFINITION'
WHERE navigation_id = '00000000-0000-0000-0000-000000000307';

-- System Settings navigation item: reuse existing SYSTEM_TELEMETRY permission
-- (Admin already has this; other roles can be managed via role/permission UI)
UPDATE cbap_navigation_items
SET required_permission = 'SYSTEM_TELEMETRY'
WHERE navigation_id = '00000000-0000-0000-0000-000000000308';

-- Org Topology navigation item should require SYSTEM_ORG_TOPOLOGY
UPDATE cbap_navigation_items
SET required_permission = 'SYSTEM_ORG_TOPOLOGY'
WHERE navigation_id = '00000000-0000-0000-0000-000000000309';

-- Licensing Status navigation item should require SYSTEM_LICENSING
UPDATE cbap_navigation_items
SET required_permission = 'SYSTEM_LICENSING'
WHERE navigation_id = '00000000-0000-0000-0000-000000000310';

