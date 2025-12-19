-- CBAP OSS - Seed Test Entity Data
-- TEMPORARY: This migration creates sample entity definitions and records for testing
-- TODO: Remove this migration once entity creation UI is implemented

-- ============================================================================
-- SAMPLE ENTITY: Product
-- ============================================================================

-- Create Product entity definition
INSERT INTO cbap_metadata_entities (
    entity_id,
    name,
    description,
    schema_version,
    screen_version,
    workflow_id,
    authorization_model,
    scope,
    created_at,
    updated_at,
    created_by
) VALUES (
    'Product',
    'Product',
    'Product catalog items',
    1,
    1,
    NULL,
    'ENTITY_LEVEL',
    'GLOBAL',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100' -- Admin user
) ON CONFLICT (entity_id) DO NOTHING;

-- Create Product properties
INSERT INTO cbap_metadata_properties (
    entity_id,
    property_name,
    property_type,
    label,
    label_key,
    required,
    read_only,
    denormalize,
    created_at,
    updated_at
) VALUES
    ('Product', 'name', 'string', 'Product Name', 'product.name', TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Product', 'description', 'string', 'Description', 'product.description', FALSE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Product', 'sku', 'string', 'SKU', 'product.sku', TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Product', 'price', 'number', 'Price', 'product.price', TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Product', 'category', 'singleSelect', 'Category', 'product.category', FALSE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Product', 'inStock', 'boolean', 'In Stock', 'product.inStock', FALSE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Product', 'stockQuantity', 'number', 'Stock Quantity', 'product.stockQuantity', FALSE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (entity_id, property_name) DO NOTHING;

-- Create sample Product records
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
    ),
    (
        'a0000000-0000-0000-0000-000000000004'::UUID,
        'Product',
        '{"name": "USB-C Hub", "description": "7-in-1 USB-C hub with HDMI", "sku": "HUB-001", "price": 49.99, "category": "Accessories", "inStock": false, "stockQuantity": 0}'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    ),
    (
        'a0000000-0000-0000-0000-000000000005'::UUID,
        'Product',
        '{"name": "Monitor 27\"", "description": "4K 27-inch monitor", "sku": "MON-001", "price": 399.99, "category": "Electronics", "inStock": true, "stockQuantity": 12}'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    )
ON CONFLICT DO NOTHING;

-- ============================================================================
-- SAMPLE ENTITY: Customer
-- ============================================================================

-- Create Customer entity definition
INSERT INTO cbap_metadata_entities (
    entity_id,
    name,
    description,
    schema_version,
    screen_version,
    workflow_id,
    authorization_model,
    scope,
    created_at,
    updated_at,
    created_by
) VALUES (
    'Customer',
    'Customer',
    'Customer information',
    1,
    1,
    NULL,
    'ENTITY_LEVEL',
    'GLOBAL',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100' -- Admin user
) ON CONFLICT (entity_id) DO NOTHING;

-- Create Customer properties
INSERT INTO cbap_metadata_properties (
    entity_id,
    property_name,
    property_type,
    label,
    label_key,
    required,
    read_only,
    denormalize,
    created_at,
    updated_at
) VALUES
    ('Customer', 'companyName', 'string', 'Company Name', 'customer.companyName', TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Customer', 'contactName', 'string', 'Contact Name', 'customer.contactName', TRUE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Customer', 'email', 'string', 'Email', 'customer.email', TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Customer', 'phone', 'string', 'Phone', 'customer.phone', FALSE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Customer', 'address', 'string', 'Address', 'customer.address', FALSE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Customer', 'city', 'string', 'City', 'customer.city', FALSE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Customer', 'country', 'string', 'Country', 'customer.country', FALSE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Customer', 'isActive', 'boolean', 'Active', 'customer.isActive', FALSE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (entity_id, property_name) DO NOTHING;

-- Create sample Customer records
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
        '{"companyName": "Acme Corporation", "contactName": "John Smith", "email": "john.smith@acme.com", "phone": "+1-555-0101", "address": "123 Main St", "city": "New York", "country": "USA", "isActive": true}'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    ),
    (
        'b0000000-0000-0000-0000-000000000002'::UUID,
        'Customer',
        '{"companyName": "Tech Solutions Inc", "contactName": "Sarah Johnson", "email": "sarah.j@techsol.com", "phone": "+1-555-0102", "address": "456 Tech Ave", "city": "San Francisco", "country": "USA", "isActive": true}'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    ),
    (
        'b0000000-0000-0000-0000-000000000003'::UUID,
        'Customer',
        '{"companyName": "Global Industries", "contactName": "Michael Brown", "email": "m.brown@global.com", "phone": "+1-555-0103", "address": "789 Business Blvd", "city": "Chicago", "country": "USA", "isActive": false}'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    ),
    (
        'b0000000-0000-0000-0000-000000000004'::UUID,
        'Customer',
        '{"companyName": "Startup Ventures", "contactName": "Emily Davis", "email": "emily@startup.com", "phone": "+1-555-0104", "address": "321 Innovation Way", "city": "Austin", "country": "USA", "isActive": true}'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    )
ON CONFLICT DO NOTHING;

-- ============================================================================
-- SAMPLE ENTITY: Order
-- ============================================================================

-- Create Order entity definition
INSERT INTO cbap_metadata_entities (
    entity_id,
    name,
    description,
    schema_version,
    screen_version,
    workflow_id,
    authorization_model,
    scope,
    created_at,
    updated_at,
    created_by
) VALUES (
    'Order',
    'Order',
    'Customer orders',
    1,
    1,
    NULL,
    'ENTITY_LEVEL',
    'GLOBAL',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100' -- Admin user
) ON CONFLICT (entity_id) DO NOTHING;

-- Create Order properties
INSERT INTO cbap_metadata_properties (
    entity_id,
    property_name,
    property_type,
    label,
    label_key,
    required,
    read_only,
    denormalize,
    created_at,
    updated_at
) VALUES
    ('Order', 'orderNumber', 'string', 'Order Number', 'order.orderNumber', TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Order', 'orderDate', 'date', 'Order Date', 'order.orderDate', TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Order', 'customerName', 'string', 'Customer', 'order.customerName', TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Order', 'totalAmount', 'number', 'Total Amount', 'order.totalAmount', TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Order', 'status', 'singleSelect', 'Status', 'order.status', TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Order', 'shippingAddress', 'string', 'Shipping Address', 'order.shippingAddress', FALSE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Order', 'notes', 'string', 'Notes', 'order.notes', FALSE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (entity_id, property_name) DO NOTHING;

-- Create sample Order records
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
        'Order',
        '{"orderNumber": "ORD-2025-001", "orderDate": "2025-01-15", "customerName": "Acme Corporation", "totalAmount": 1299.99, "status": "Pending", "shippingAddress": "123 Main St, New York, USA", "notes": "Rush delivery requested"}'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    ),
    (
        'c0000000-0000-0000-0000-000000000002'::UUID,
        'Order',
        '{"orderNumber": "ORD-2025-002", "orderDate": "2025-01-16", "customerName": "Tech Solutions Inc", "totalAmount": 89.99, "status": "Shipped", "shippingAddress": "456 Tech Ave, San Francisco, USA", "notes": ""}'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    ),
    (
        'c0000000-0000-0000-0000-000000000003'::UUID,
        'Order',
        '{"orderNumber": "ORD-2025-003", "orderDate": "2025-01-17", "customerName": "Startup Ventures", "totalAmount": 449.98, "status": "Delivered", "shippingAddress": "321 Innovation Way, Austin, USA", "notes": "Gift wrapping included"}'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    ),
    (
        'c0000000-0000-0000-0000-000000000004'::UUID,
        'Order',
        '{"orderNumber": "ORD-2025-004", "orderDate": "2025-01-18", "customerName": "Acme Corporation", "totalAmount": 29.99, "status": "Pending", "shippingAddress": "123 Main St, New York, USA", "notes": ""}'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    ),
    (
        'c0000000-0000-0000-0000-000000000005'::UUID,
        'Order',
        '{"orderNumber": "ORD-2025-005", "orderDate": "2025-01-19", "customerName": "Tech Solutions Inc", "totalAmount": 399.99, "status": "Processing", "shippingAddress": "456 Tech Ave, San Francisco, USA", "notes": "International shipping"}'::jsonb,
        1,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000100'::UUID
    )
ON CONFLICT DO NOTHING;
