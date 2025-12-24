-- CBAP OSS - Remove Organization Topology Feature
-- Organization topology management is a commercial-only feature.
-- OSS version runs on a single site and does not support multi-site deployments.

-- Remove the Org Topology navigation item from Administration section
DELETE FROM cbap_navigation_items
WHERE navigation_id = '00000000-0000-0000-0000-000000000309';
