-- CBAP OSS - Create Country Entity and Update Order for Measures Testing
-- Creates Country entity, updates Customer to reference Country, and updates Order/OrderLineItem
-- to use calculated fields with measures

-- ============================================================================
-- CREATE COUNTRY ENTITY
-- ============================================================================

-- Create Country entity definition
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
    'Country',
    'Country',
    'Country information with tax rates',
    1,
    1,
    NULL,
    'ENTITY_LEVEL',
    'GLOBAL',
    '{"displayField": "countryName", "searchDisplay": "countryCode"}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100' -- Admin user
) ON CONFLICT (entity_id) DO NOTHING;

-- Create Country properties
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
    ('Country', 'countryCode', 'string', 'Country Code', 'country.countryCode', TRUE, FALSE, TRUE, NULL, '{"indexable": true}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Country', 'countryName', 'string', 'Country Name', 'country.countryName', TRUE, FALSE, TRUE, NULL, '{"indexable": true}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Country', 'federalTax', 'number', 'Federal Tax %', 'country.federalTax', TRUE, FALSE, TRUE, NULL, '{"indexable": true}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (entity_id, property_name) DO NOTHING;

-- Seed USA country data
-- Use INSERT ... ON CONFLICT to handle case where record already exists
INSERT INTO cbap_entity_records (
    record_id,
    entity_id,
    data_json,
    schema_version,
    state,
    created_at,
    updated_at,
    created_by
) VALUES (
    'c0000000-0000-0000-0000-000000000001'::UUID,
    'Country',
    '{
        "countryCode": "USA",
        "countryName": "United States of America",
        "federalTax": 7.5
    }'::jsonb,
    1,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100'::UUID
) ON CONFLICT (record_id) DO NOTHING;

-- ============================================================================
-- UPDATE CUSTOMER ENTITY TO REFERENCE COUNTRY
-- ============================================================================

-- Add country property to Customer entity
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
) VALUES (
    'Customer',
    'country',
    'reference',
    'Country',
    'customer.country',
    TRUE,
    FALSE,
    TRUE,
    'Country',
    '{"indexable": true, "displayField": "countryName"}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (entity_id, property_name) DO NOTHING;

-- Update existing Customer records to reference USA
UPDATE cbap_entity_records
SET data_json = jsonb_set(
    COALESCE(data_json, '{}'::jsonb),
    '{country}',
    '"c0000000-0000-0000-0000-000000000001"'
)
WHERE entity_id = 'Customer'
  AND data_json->>'country' IS NULL;

-- ============================================================================
-- UPDATE ORDERLINEITEM ENTITY
-- ============================================================================

-- Make unitPrice read-only (it will be auto-populated from Product)
-- Use metadata to define auto-population rule
UPDATE cbap_metadata_properties
SET read_only = TRUE,
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"autoPopulateFrom": "product", "sourceField": "price"}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'OrderLineItem' AND property_name = 'unitPrice';

-- Add taxPercent property (calculated - uses function to lookup country tax from customer)
-- The expression uses lookupCountryTax function which resolves Customer -> Country -> federalTax
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
) VALUES (
    'OrderLineItem',
    'taxPercent',
    'calculated',
    'Tax %',
    'orderLineItem.taxPercent',
    FALSE,
    TRUE,
    TRUE,
    NULL,
    '{"expression": "lookupCountryTax($parent.customer)", "note": "Tax % calculated from customer country via lookup function"}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (entity_id, property_name) DO NOTHING;

-- Add taxValue property (calculated: unitPrice * quantity * taxPercent / 100)
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
) VALUES (
    'OrderLineItem',
    'taxValue',
    'calculated',
    'Tax Value',
    'orderLineItem.taxValue',
    FALSE,
    TRUE,
    TRUE,
    NULL,
    '{"expression": "(unitPrice * quantity * taxPercent) / 100"}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (entity_id, property_name) DO NOTHING;

-- Update total calculation to include tax: (unitPrice * quantity) + taxValue
-- Also ensure it's marked as calculated type
UPDATE cbap_metadata_properties
SET property_type = 'calculated',
    read_only = TRUE,
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"expression": "(unitPrice * quantity) + taxValue"}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'OrderLineItem' AND property_name = 'total';

-- ============================================================================
-- UPDATE ORDER ENTITY
-- ============================================================================

-- Make totalAmount calculated and read-only (sum of line item totals)
UPDATE cbap_metadata_properties
SET property_type = 'calculated',
    read_only = TRUE,
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"expression": "sum(lineItems.total)"}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Order' AND property_name = 'totalAmount';

-- ============================================================================
-- UPDATE LINEITEMS PROPERTY WITH DETAIL VIEW CONFIGURATION (COLUMN ORDER)
-- ============================================================================

-- Update lineItems property with detail view configuration including new columns
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{
        "isDetailEntityArray": true,
        "detailEntityId": "OrderLineItem",
        "minItems": 1,
        "detailView": {
            "columnOrder": ["product", "quantity", "unitPrice", "taxPercent", "taxValue", "total"],
            "columnConfig": {
                "product": {
                    "displayType": "reference",
                    "displayField": "name"
                },
                "unitPrice": {
                    "format": "currency",
                    "readOnly": true
                },
                "taxPercent": {
                    "displayType": "calculated",
                    "format": "percentage"
                },
                "taxValue": {
                    "displayType": "calculated",
                    "format": "currency"
                },
                "total": {
                    "displayType": "calculated",
                    "format": "currency"
                }
            }
        }
    }'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Order' 
  AND property_name = 'lineItems';

-- ============================================================================
-- CREATE MEASURE FOR COUNTRY TAX RATE
-- ============================================================================

-- Note: Measures for cross-entity lookups (like country.taxRate) will be implemented
-- when the expression evaluator supports cross-entity resolution functions.
-- For now, taxPercent calculation will be handled via backend service that resolves
-- Customer -> Country -> federalTax lookup.

-- ============================================================================
-- NOTES
-- ============================================================================
-- 1. Country entity stores country code, name, and federal tax rate
-- 2. Customer now references Country
-- 3. OrderLineItem unitPrice is read-only and auto-populated from Product (via metadata)
-- 4. OrderLineItem has calculated fields with expressions in metadata:
--    - taxPercent: uses measure("country.taxRate", {customerId: $parent.customer})
--    - taxValue: (unitPrice * quantity * taxPercent) / 100
--    - total: (unitPrice * quantity) + taxValue
-- 5. Order totalAmount is calculated as sum(lineItems.total) and is read-only
-- 6. Column order for lineItems is defined in metadata (detailView.columnOrder)
-- 7. All calculations are metadata-driven, no hardcoded business logic
-- ============================================================================
