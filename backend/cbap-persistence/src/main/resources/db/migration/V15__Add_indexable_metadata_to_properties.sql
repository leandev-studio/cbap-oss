-- CBAP OSS - Add Indexable Metadata to Properties
-- Adds explicit indexing configuration to properties via metadata_json

-- ============================================================================
-- UPDATE PROPERTIES WITH INDEXABLE METADATA
-- ============================================================================

-- For Order entity: mark indexable fields
-- Order Number, Customer (will index customer name), Status, Order Date, Total Amount
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Order' 
  AND property_name IN ('orderNumber', 'customer', 'status', 'orderDate', 'totalAmount');
  
-- Note: For 'customer' reference field, the system will automatically fetch and index
-- the customer's companyName (or name) from the referenced Customer record

-- For Customer entity: mark indexable fields
-- Company Name, Email (already have denormalize=true, but make it explicit)
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Customer' 
  AND property_name IN ('companyName', 'email', 'contactName');

-- For Product entity: mark indexable fields
-- Name, SKU, Price, Category (already have denormalize=true, but make it explicit)
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"indexable": true}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Product' 
  AND property_name IN ('name', 'sku', 'price', 'category');

-- For OrderLineItem entity: mark indexable fields if any
-- (Product reference will be indexed via Order's customer indexing logic)

-- ============================================================================
-- NOTE: Properties with indexable=true in metadata_json will be indexed
-- regardless of the denormalize flag. This gives explicit control over
-- what gets indexed for search.
-- ============================================================================
