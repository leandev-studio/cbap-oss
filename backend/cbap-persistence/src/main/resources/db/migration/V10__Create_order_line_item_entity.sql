-- CBAP OSS - Create OrderLineItem Entity for Master-Detail Pattern
-- Creates OrderLineItem entity definition and updates Order entity

-- ============================================================================
-- CREATE ORDER LINE ITEM ENTITY
-- ============================================================================

-- Create OrderLineItem entity definition
INSERT INTO cbap_metadata_entities (
    entity_id,
    name,
    description,
    schema_version,
    screen_version,
    workflow_id,
    authorization_model,
    scope,
    metadata_json,
    created_at,
    updated_at,
    created_by
) VALUES (
    'OrderLineItem',
    'Order Line Item',
    'Line items for orders (master-detail relationship)',
    1,
    1,
    NULL,
    'ENTITY_LEVEL',
    'GLOBAL',
    '{"isDetailEntity": true, "masterEntity": "Order"}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100' -- Admin user
) ON CONFLICT (entity_id) DO NOTHING;

-- Create OrderLineItem properties
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
    metadata_json,
    created_at,
    updated_at
) VALUES
    ('OrderLineItem', 'product', 'reference', 'Product', 'lineItem.product', TRUE, FALSE, TRUE, 'Product', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('OrderLineItem', 'unitPrice', 'number', 'Unit Price', 'lineItem.unitPrice', TRUE, FALSE, TRUE, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('OrderLineItem', 'quantity', 'number', 'Quantity', 'lineItem.quantity', TRUE, FALSE, TRUE, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('OrderLineItem', 'total', 'calculated', 'Total', 'lineItem.total', FALSE, TRUE, TRUE, NULL, '{"expression": "unitPrice * quantity"}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (entity_id, property_name) DO NOTHING;

-- ============================================================================
-- UPDATE ORDER ENTITY
-- ============================================================================

-- Remove products multiSelect property (replaced with lineItems)
DELETE FROM cbap_metadata_properties 
WHERE entity_id = 'Order' AND property_name = 'products';

-- Add lineItems property (will store array of OrderLineItem objects as JSONB)
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
    'lineItems',
    'string', -- Using string type but with special metadata to indicate it's a nested entity array
    'Line Items',
    'order.lineItems',
    FALSE, -- Not required at Order level, but line items should have at least one
    FALSE,
    FALSE,
    '{"isDetailEntityArray": true, "detailEntityId": "OrderLineItem", "minItems": 1}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (entity_id, property_name) DO UPDATE
SET 
    property_type = 'string',
    metadata_json = '{"isDetailEntityArray": true, "detailEntityId": "OrderLineItem", "minItems": 1}'::jsonb,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================================
-- UPDATE EXISTING ORDER RECORDS
-- ============================================================================

-- Convert existing products arrays to lineItems with sample data
-- Order 1: Laptop Pro 15 (price: 1299.99)
UPDATE cbap_entity_records
SET data_json = jsonb_set(
    data_json - 'products',
    '{lineItems}',
    '[{"product": "a0000000-0000-0000-0000-000000000001", "unitPrice": 1299.99, "quantity": 1, "total": 1299.99}]'::jsonb
)
WHERE entity_id = 'Order' 
  AND record_id = 'c0000000-0000-0000-0000-000000000001'::UUID;

-- Order 2: Mechanical Keyboard (price: 89.99)
UPDATE cbap_entity_records
SET data_json = jsonb_set(
    data_json - 'products',
    '{lineItems}',
    '[{"product": "a0000000-0000-0000-0000-000000000003", "unitPrice": 89.99, "quantity": 1, "total": 89.99}]'::jsonb
)
WHERE entity_id = 'Order' 
  AND record_id = 'c0000000-0000-0000-0000-000000000002'::UUID;

-- Order 3: Monitor 27" (price: 399.99) and USB-C Hub (price: 49.99)
UPDATE cbap_entity_records
SET data_json = jsonb_set(
    data_json - 'products',
    '{lineItems}',
    '[
        {"product": "a0000000-0000-0000-0000-000000000005", "unitPrice": 399.99, "quantity": 1, "total": 399.99},
        {"product": "a0000000-0000-0000-0000-000000000004", "unitPrice": 49.99, "quantity": 1, "total": 49.99}
    ]'::jsonb
)
WHERE entity_id = 'Order' 
  AND record_id = 'c0000000-0000-0000-0000-000000000003'::UUID;

-- Order 4: Wireless Mouse (price: 29.99)
UPDATE cbap_entity_records
SET data_json = jsonb_set(
    data_json - 'products',
    '{lineItems}',
    '[{"product": "a0000000-0000-0000-0000-000000000002", "unitPrice": 29.99, "quantity": 1, "total": 29.99}]'::jsonb
)
WHERE entity_id = 'Order' 
  AND record_id = 'c0000000-0000-0000-0000-000000000004'::UUID;

-- Order 5: Monitor 27" (price: 399.99)
UPDATE cbap_entity_records
SET data_json = jsonb_set(
    data_json - 'products',
    '{lineItems}',
    '[{"product": "a0000000-0000-0000-0000-000000000005", "unitPrice": 399.99, "quantity": 1, "total": 399.99}]'::jsonb
)
WHERE entity_id = 'Order' 
  AND record_id = 'c0000000-0000-0000-0000-000000000005'::UUID;
