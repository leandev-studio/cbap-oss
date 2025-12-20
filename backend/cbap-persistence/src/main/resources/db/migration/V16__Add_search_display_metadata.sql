-- CBAP OSS - Add Search Display Metadata
-- Adds metadata configuration for what to display in global search results

-- ============================================================================
-- UPDATE ENTITY DEFINITIONS WITH SEARCH DISPLAY CONFIGURATION
-- ============================================================================

-- For Order entity: display orderNumber in global search
UPDATE cbap_metadata_entities
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{
        "searchDisplay": "orderNumber"
    }'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Order';

-- For Customer entity: display companyName in global search
UPDATE cbap_metadata_entities
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{
        "searchDisplay": "companyName"
    }'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Customer';

-- For Product entity: display name in global search
UPDATE cbap_metadata_entities
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{
        "searchDisplay": "name"
    }'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Product';

-- ============================================================================
-- NOTE: searchDisplay can be:
-- - A single field name: "orderNumber"
-- - Multiple fields separated by "|": "orderNumber | customer"
-- - A computed expression (future): "orderNumber + ' - ' + customer"
-- ============================================================================
