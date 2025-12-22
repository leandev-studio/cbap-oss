-- CBAP OSS - Add Invoice Validation Rules
-- Adds validation rules for Invoice entity
-- 
-- NOTE: These are example validation rules for testing the validation engine.
-- In a real deployment, validation rules would be created via the metadata API
-- or configuration UI, not hardcoded in migrations. This migration serves as
-- a reference implementation.

-- ============================================================================
-- INVOICE VALIDATION RULES
-- ============================================================================

-- Rule 1: Invoice Total Amount must be >= 0 (field-level range validation)
-- This is a field-level validation rule using the RANGE rule type.
INSERT INTO cbap_metadata_validation_rules (
    validation_id,
    entity_id,
    property_name,
    rule_name,
    description,
    scope,
    rule_type,
    expression,
    error_message,
    error_message_key,
    trigger_events,
    conditions_json,
    metadata_json,
    schema_version,
    created_at,
    updated_at,
    created_by
) VALUES (
    uuid_generate_v4(),
    'Invoice',
    'amount',
    'Invoice Amount Must Be Non-Negative',
    'Validates that the invoice total amount is not less than zero',
    'FIELD',
    'RANGE',
    NULL, -- RANGE type uses metadata_json for min/max
    'Total Amount cannot be less than zero',
    'invoice.validation.amountNonNegative',
    '["CREATE", "UPDATE"]'::jsonb,
    NULL,
    '{"min": 0}'::jsonb,
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100' -- Admin user
) ON CONFLICT DO NOTHING;

-- Rule 2: Invoice Total Amount must be greater than or equal to sum of line item totals
-- This is an entity-level validation rule that checks the relationship between
-- the invoice amount and the sum of all line item totals.
-- NOTE: This rule uses the sum() function which requires Measures/Calculations to be fully implemented.
-- For now, this serves as a reference for how such rules would be defined.
INSERT INTO cbap_metadata_validation_rules (
    validation_id,
    entity_id,
    property_name,
    rule_name,
    description,
    scope,
    rule_type,
    expression,
    error_message,
    error_message_key,
    trigger_events,
    conditions_json,
    metadata_json,
    schema_version,
    created_at,
    updated_at,
    created_by
) VALUES (
    uuid_generate_v4(),
    'Invoice',
    NULL, -- Entity-level rule, no specific property
    'Invoice Amount Must Be Greater Than Or Equal To Line Items Total',
    'Validates that the invoice total amount is at least equal to the sum of all line item totals',
    'ENTITY',
    'EXPRESSION',
    'amount >= sum(lineItems.total)',
    'Total Amount must be greater than or equal to the sum of line item totals',
    'invoice.validation.amountGreaterThanOrEqualLineItemsTotal',
    '["CREATE", "UPDATE"]'::jsonb,
    NULL,
    NULL,
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100' -- Admin user
) ON CONFLICT DO NOTHING;

-- ============================================================================
-- NOTES
-- ============================================================================
-- This validation rule uses the CEL-v0 expression: amount >= sum(lineItems.total)
-- The sum() function is supported by the ExpressionEvaluator to sum a field
-- across all items in an array.
-- ============================================================================
