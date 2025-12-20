-- CBAP OSS - Update Order Entity for Testing
-- Updates Order entity to use reference fields for customer and products

-- ============================================================================
-- UPDATE ORDER PROPERTIES
-- ============================================================================

-- Remove old customerName property (string)
DELETE FROM cbap_metadata_properties 
WHERE entity_id = 'Order' AND property_name = 'customerName';

-- Add customer property as reference to Customer entity
INSERT INTO cbap_metadata_properties (
    entity_id,
    property_name,
    property_type,
    label,
    label_key,
    required,
    read_only,
    denormalize,
    reference_entity_id,
    created_at,
    updated_at
) VALUES (
    'Order',
    'customer',
    'reference',
    'Customer',
    'order.customer',
    TRUE,
    FALSE,
    TRUE,
    'Customer', -- Reference to Customer entity
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (entity_id, property_name) DO UPDATE
SET 
    property_type = 'reference',
    reference_entity_id = 'Customer',
    updated_at = CURRENT_TIMESTAMP;

-- Add products property as multiSelect (will store array of product record IDs)
-- Note: For now, we'll use multiSelect with metadata indicating it should fetch from Product entity
-- In the future, we might add a 'multiReference' type, but for now multiSelect with dynamic options works
INSERT INTO cbap_metadata_properties (
    entity_id,
    property_name,
    property_type,
    label,
    label_key,
    required,
    read_only,
    denormalize,
    metadata_json,
    created_at,
    updated_at
) VALUES (
    'Order',
    'products',
    'multiSelect',
    'Products',
    'order.products',
    TRUE,
    FALSE,
    FALSE,
    '{"referenceEntity": "Product", "fetchOptionsFromEntity": true}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (entity_id, property_name) DO UPDATE
SET 
    property_type = 'multiSelect',
    metadata_json = '{"referenceEntity": "Product", "fetchOptionsFromEntity": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================================
-- UPDATE EXISTING ORDER RECORDS
-- ============================================================================

-- Update Order records to use customer reference (UUID) instead of customerName (string)
-- Map customer names to Customer record IDs
UPDATE cbap_entity_records
SET data_json = jsonb_set(
    jsonb_set(
        data_json - 'customerName',
        '{customer}',
        CASE 
            WHEN data_json->>'customerName' = 'Acme Corporation' THEN '"b0000000-0000-0000-0000-000000000001"'
            WHEN data_json->>'customerName' = 'Tech Solutions Inc' THEN '"b0000000-0000-0000-0000-000000000002"'
            WHEN data_json->>'customerName' = 'Global Industries' THEN '"b0000000-0000-0000-0000-000000000003"'
            WHEN data_json->>'customerName' = 'Startup Ventures' THEN '"b0000000-0000-0000-0000-000000000004"'
            ELSE 'null'::jsonb
        END::jsonb
    ),
    '{products}',
    '[]'::jsonb -- Initialize empty products array
)
WHERE entity_id = 'Order'
  AND data_json ? 'customerName';

-- Add sample products to existing orders
-- Order 1: Laptop Pro 15
UPDATE cbap_entity_records
SET data_json = jsonb_set(
    data_json,
    '{products}',
    '["a0000000-0000-0000-0000-000000000001"]'::jsonb
)
WHERE entity_id = 'Order' 
  AND record_id = 'c0000000-0000-0000-0000-000000000001'::UUID;

-- Order 2: Mechanical Keyboard
UPDATE cbap_entity_records
SET data_json = jsonb_set(
    data_json,
    '{products}',
    '["a0000000-0000-0000-0000-000000000003"]'::jsonb
)
WHERE entity_id = 'Order' 
  AND record_id = 'c0000000-0000-0000-0000-000000000002'::UUID;

-- Order 3: Monitor 27" and USB-C Hub
UPDATE cbap_entity_records
SET data_json = jsonb_set(
    data_json,
    '{products}',
    '["a0000000-0000-0000-0000-000000000005", "a0000000-0000-0000-0000-000000000004"]'::jsonb
)
WHERE entity_id = 'Order' 
  AND record_id = 'c0000000-0000-0000-0000-000000000003'::UUID;

-- Order 4: Wireless Mouse
UPDATE cbap_entity_records
SET data_json = jsonb_set(
    data_json,
    '{products}',
    '["a0000000-0000-0000-0000-000000000002"]'::jsonb
)
WHERE entity_id = 'Order' 
  AND record_id = 'c0000000-0000-0000-0000-000000000004'::UUID;

-- Order 5: Monitor 27"
UPDATE cbap_entity_records
SET data_json = jsonb_set(
    data_json,
    '{products}',
    '["a0000000-0000-0000-0000-000000000005"]'::jsonb
)
WHERE entity_id = 'Order' 
  AND record_id = 'c0000000-0000-0000-0000-000000000005'::UUID;
