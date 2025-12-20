-- CBAP OSS - Mark Properties as Indexable
-- Marks entity properties as indexable for search functionality

-- ============================================================================
-- ORDER ENTITY PROPERTIES
-- ============================================================================

-- Mark orderNumber as indexable
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Order' AND property_name = 'orderNumber';

-- Mark customer (reference) as indexable (will index display value)
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Order' AND property_name = 'customer';

-- Mark orderDate as indexable
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Order' AND property_name = 'orderDate';

-- Mark status as indexable
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Order' AND property_name = 'status';

-- Mark totalAmount as indexable
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Order' AND property_name = 'totalAmount';

-- ============================================================================
-- INVOICE ENTITY PROPERTIES
-- ============================================================================

-- Mark invoiceNumber as indexable
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Invoice' AND property_name = 'invoiceNumber';

-- Mark vendor (reference) as indexable (will index display value)
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Invoice' AND property_name = 'vendor';

-- Mark invoiceDate as indexable
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Invoice' AND property_name = 'invoiceDate';

-- Mark dueDate as indexable
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Invoice' AND property_name = 'dueDate';

-- Mark amount as indexable
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Invoice' AND property_name = 'amount';

-- ============================================================================
-- CUSTOMER ENTITY PROPERTIES
-- ============================================================================

-- Mark companyName as indexable
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Customer' AND property_name = 'companyName';

-- Mark contactName as indexable
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Customer' AND property_name = 'contactName';

-- Mark email as indexable
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Customer' AND property_name = 'email';

-- ============================================================================
-- PRODUCT ENTITY PROPERTIES
-- ============================================================================

-- Mark name as indexable
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Product' AND property_name = 'name';

-- Mark sku as indexable
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Product' AND property_name = 'sku';

-- Mark description as indexable
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Product' AND property_name = 'description';
