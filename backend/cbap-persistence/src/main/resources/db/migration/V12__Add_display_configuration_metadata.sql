-- CBAP OSS - Add Display Configuration Metadata
-- Adds metadata-driven display configuration to remove hardcoded business logic

-- ============================================================================
-- UPDATE REFERENCE PROPERTIES WITH DISPLAY FIELD CONFIGURATION
-- ============================================================================

-- Update Customer reference in Order entity to specify displayField
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"displayField": "companyName"}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Order' 
  AND property_name = 'customer'
  AND property_type = 'reference';

-- Update Product reference in OrderLineItem entity to specify displayField
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"displayField": "name"}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'OrderLineItem' 
  AND property_name = 'product'
  AND property_type = 'reference';

-- Also update any other Product references that might exist
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{"displayField": "name"}'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE reference_entity_id = 'Product'
  AND property_type = 'reference'
  AND (metadata_json IS NULL OR metadata_json->>'displayField' IS NULL);

-- ============================================================================
-- UPDATE ENTITY DEFINITIONS WITH LIST VIEW CONFIGURATION
-- ============================================================================

-- Update Order entity with list view column configuration
UPDATE cbap_metadata_entities
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{
        "listView": {
            "columnOrder": ["orderNumber", "customer", "lineItems", "status", "totalAmount", "orderDate", "shippingAddress", "notes"],
            "columnConfig": {
                "lineItems": {
                    "displayType": "count",
                    "label": "Line Items"
                },
                "customer": {
                    "displayType": "reference",
                    "displayField": "companyName"
                }
            }
        }
    }'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Order';

-- ============================================================================
-- UPDATE PROPERTY DEFINITIONS WITH DETAIL VIEW CONFIGURATION
-- ============================================================================

-- Update lineItems property with detail view configuration
UPDATE cbap_metadata_properties
SET 
    metadata_json = COALESCE(metadata_json, '{}'::jsonb) || '{
        "isDetailEntityArray": true,
        "detailEntityId": "OrderLineItem",
        "minItems": 1,
        "detailView": {
            "columnOrder": ["product", "quantity", "unitPrice", "total"],
            "columnConfig": {
                "product": {
                    "displayType": "reference",
                    "displayField": "name"
                },
                "total": {
                    "displayType": "calculated",
                    "format": "currency"
                },
                "unitPrice": {
                    "format": "currency"
                }
            }
        }
    }'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Order' 
  AND property_name = 'lineItems';
