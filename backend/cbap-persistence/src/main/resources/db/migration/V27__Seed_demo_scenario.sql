-- CBAP OSS - V27 Seed Demo Scenario
-- Clean out old demo data and seed a consistent scenario for:
-- Country, Customer/Vendor, Product, Order/OrderLineItem, Invoice/InvoiceLineItem.
--
-- This migration is **demo/data only**. It does not create or alter tables.
-- It assumes all schema and metadata migrations (V1–V26) have already run.
-- In production, you can choose to skip this migration if you do not want demo data.

-- ============================================================================
-- 0. CLEAN EXISTING DEMO DATA (RECORDS ONLY, NOT METADATA)
-- ============================================================================

DELETE FROM cbap_entity_records
WHERE entity_id IN (
    'Country',
    'Customer',
    'Product',
    'Order',
    'OrderLineItem',
    'Invoice',
    'InvoiceLineItem'
);

-- ============================================================================
-- 1. COUNTRY SEED DATA
-- ============================================================================

-- Seed canonical country records
INSERT INTO cbap_entity_records (
    record_id,
    entity_id,
    data_json,
    schema_version,
    state,
    created_at,
    updated_at,
    created_by
) VALUES
    (
        'c0000000-0000-0000-0000-000000000001'::UUID,
        'Country',
        '{"countryCode": "USA", "countryName": "United States of America", "federalTax": 7.5}'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    )
ON CONFLICT (record_id) DO NOTHING;

-- ============================================================================
-- 2. CUSTOMER / VENDOR SEED DATA
--    Vendors are represented by the Customer entity and referenced by Invoice.vendor
-- ============================================================================

INSERT INTO cbap_entity_records (
    record_id,
    entity_id,
    data_json,
    schema_version,
    state,
    created_at,
    updated_at,
    created_by
) VALUES
    (
        'b0000000-0000-0000-0000-000000000001'::UUID,
        'Customer',
        '{
            "companyName": "Acme Corporation",
            "contactName": "John Smith",
            "email": "john.smith@acme.com",
            "phone": "+1-555-0101",
            "address": "123 Main St",
            "city": "New York",
            "country": "c0000000-0000-0000-0000-000000000001",
            "isActive": true
        }'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    ),
    (
        'b0000000-0000-0000-0000-000000000002'::UUID,
        'Customer',
        '{
            "companyName": "Tech Solutions Inc",
            "contactName": "Sarah Johnson",
            "email": "sarah.j@techsol.com",
            "phone": "+1-555-0102",
            "address": "456 Tech Ave",
            "city": "San Francisco",
            "country": "c0000000-0000-0000-0000-000000000001",
            "isActive": true
        }'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    )
ON CONFLICT (record_id) DO NOTHING;

-- ============================================================================
-- 3. PRODUCT SEED DATA (CATALOG)
-- ============================================================================

INSERT INTO cbap_entity_records (
    record_id,
    entity_id,
    data_json,
    schema_version,
    state,
    created_at,
    updated_at,
    created_by
) VALUES
    (
        'a0000000-0000-0000-0000-000000000001'::UUID,
        'Product',
        '{"name": "Laptop Pro 15", "description": "High-performance laptop with 15-inch display", "sku": "LAP-001", "price": 1299.99, "category": "Electronics", "inStock": true, "stockQuantity": 25}'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    ),
    (
        'a0000000-0000-0000-0000-000000000002'::UUID,
        'Product',
        '{"name": "Wireless Mouse", "description": "Ergonomic wireless mouse", "sku": "MOU-001", "price": 29.99, "category": "Accessories", "inStock": true, "stockQuantity": 150}'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    ),
    (
        'a0000000-0000-0000-0000-000000000003'::UUID,
        'Product',
        '{"name": "Mechanical Keyboard", "description": "RGB mechanical keyboard", "sku": "KEY-001", "price": 89.99, "category": "Accessories", "inStock": true, "stockQuantity": 45}'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    )
ON CONFLICT (record_id) DO NOTHING;

-- ============================================================================
-- 4. ORDER / ORDER LINE ITEMS
--    Assumes Order + OrderLineItem metadata (incl. calculated fields) already exist.
--    totalAmount is calculated via metadata expression sum(lineItems.total).
-- ============================================================================

INSERT INTO cbap_entity_records (
    record_id,
    entity_id,
    data_json,
    schema_version,
    state,
    created_at,
    updated_at,
    created_by
) VALUES
    (
        'c0000000-0000-0000-0000-000000000101'::UUID,
        'Order',
        '{
            "orderNumber": "ORD-2025-100",
            "orderDate": "2025-01-15",
            "customer": "b0000000-0000-0000-0000-000000000001",
            "status": "Pending",
            "shippingAddress": "123 Main St, New York, USA",
            "notes": "Rush delivery requested",
            "lineItems": [
                {
                    "product": "a0000000-0000-0000-0000-000000000001",
                    "unitPrice": 1299.99,
                    "quantity": 11,
                    "taxPercent": 7.5,
                    "taxValue": 1074.91,
                    "total": 15373.80
                }
            ]
        }'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    )
ON CONFLICT (record_id) DO NOTHING;

-- ============================================================================
-- 5. INVOICE / INVOICE LINE ITEMS
--    Uses existing Invoice / InvoiceLineItem metadata and workflow.
--    Invoice.amount is validated against sum(lineItems.total) via validation rules.
-- ============================================================================

INSERT INTO cbap_entity_records (
    record_id,
    entity_id,
    data_json,
    schema_version,
    state,
    created_at,
    updated_at,
    created_by
) VALUES
    (
        'd0000000-0000-0000-0000-000000000101'::UUID,
        'Invoice',
        '{
            "invoiceNumber": "INV-2025-100",
            "vendor": "b0000000-0000-0000-0000-000000000001",
            "invoiceDate": "2025-01-15",
            "dueDate": "2025-02-15",
            "amount": 1250.00,
            "description": "Office supplies and equipment",
            "lineItems": [
                {"description": "Office chairs", "quantity": 5, "unitPrice": 150.00, "total": 750.00},
                {"description": "Desk organizers", "quantity": 10, "unitPrice": 25.00, "total": 250.00},
                {"description": "Printer paper", "quantity": 20, "unitPrice": 12.50, "total": 250.00}
            ]
        }'::jsonb,
        1,
        'create',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    ),
    (
        'd0000000-0000-0000-0000-000000000102'::UUID,
        'Invoice',
        '{
            "invoiceNumber": "INV-2025-101",
            "vendor": "b0000000-0000-0000-0000-000000000002",
            "invoiceDate": "2025-01-20",
            "dueDate": "2025-02-20",
            "amount": 3500.00,
            "description": "IT consulting services",
            "lineItems": [
                {"description": "System integration", "quantity": 20, "teenitPrice": 150.00, "total": 3000.00},
                {"description": "Training sessions", "quantity": 5, "unitPrice": 100.00, "total": 500.00}
            ]
        }'::jsonb,
        1,
        'submit_for_approval',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    )
ON CONFLICT (record_id) DO NOTHING;

-- ============================================================================
-- END: V27__Seed_demo_scenario.sql
-- ============================================================================

