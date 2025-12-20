-- CBAP OSS - Add Order Status Options
-- Updates Order status property to include selectable status values

-- ============================================================================
-- UPDATE ORDER STATUS PROPERTY WITH OPTIONS
-- ============================================================================

-- Update the status property to include options in metadata_json
UPDATE cbap_metadata_properties
SET 
    metadata_json = '{
        "options": [
            {"value": "Pending", "label": "Pending"},
            {"value": "Processing", "label": "Processing"},
            {"value": "Shipped", "label": "Shipped"},
            {"value": "Delivered", "label": "Delivered"},
            {"value": "Cancelled", "label": "Cancelled"},
            {"value": "On Hold", "label": "On Hold"}
        ]
    }'::jsonb,
    updated_at = CURRENT_TIMESTAMP
WHERE entity_id = 'Order' 
  AND property_name = 'status';

-- Verify the update
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM cbap_metadata_properties 
        WHERE entity_id = 'Order' 
          AND property_name = 'status'
          AND metadata_json IS NOT NULL
    ) THEN
        RAISE EXCEPTION 'Failed to update Order status property with options';
    END IF;
END $$;
