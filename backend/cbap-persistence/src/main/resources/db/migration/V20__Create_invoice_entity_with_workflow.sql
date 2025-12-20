-- CBAP OSS - Create Invoice Entity with Accounts Payable Workflow
-- Creates Invoice entity, InvoiceLineItem entity, workflow, and seed data

-- ============================================================================
-- CREATE INVOICE WORKFLOW
-- ============================================================================

-- Workflow Definition: Invoice Approval Workflow
INSERT INTO cbap_metadata_workflows (
    workflow_id,
    name,
    description,
    initial_state,
    metadata_json,
    created_at,
    updated_at,
    created_by
) VALUES (
    'InvoiceApprovalWorkflow',
    'Invoice Approval Workflow',
    'Accounts payable workflow: create -> submit for approval -> approve -> pay -> close',
    'create',
    '{"category": "accounts_payable", "version": 1}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100' -- Admin user
) ON CONFLICT (workflow_id) DO NOTHING;

-- Workflow States
INSERT INTO cbap_metadata_workflow_states (
    workflow_id,
    state_name,
    label,
    label_key,
    description,
    is_initial,
    is_final,
    metadata_json,
    created_at,
    updated_at
) VALUES
    ('InvoiceApprovalWorkflow', 'create', 'Draft', 'invoice.state.create', 'Invoice is being created', TRUE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('InvoiceApprovalWorkflow', 'submit_for_approval', 'Pending Approval', 'invoice.state.submit_for_approval', 'Invoice submitted and awaiting approval', FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('InvoiceApprovalWorkflow', 'approve', 'Approved', 'invoice.state.approve', 'Invoice has been approved', FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('InvoiceApprovalWorkflow', 'pay', 'Paid', 'invoice.state.pay', 'Invoice has been paid', FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('InvoiceApprovalWorkflow', 'close', 'Closed', 'invoice.state.close', 'Invoice is closed', FALSE, TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (workflow_id, state_name) DO NOTHING;

-- Workflow Transitions
INSERT INTO cbap_metadata_workflow_transitions (
    workflow_id,
    from_state,
    to_state,
    action_label,
    label_key,
    description,
    allowed_roles,
    metadata_json,
    created_at,
    updated_at
) VALUES
    -- create -> submit_for_approval
    (
        'InvoiceApprovalWorkflow',
        'create',
        'submit_for_approval',
        'Submit for Approval',
        'invoice.transition.submit',
        'Submit invoice for approval',
        '["User", "Admin"]'::jsonb,
        '{"createTasks": {"tasks": [{"assigneeId": "00000000-0000-0000-0000-000000000100", "title": "Review Invoice", "description": "Please review and approve this invoice", "priority": "HIGH"}]}}'::jsonb,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    -- submit_for_approval -> approve
    (
        'InvoiceApprovalWorkflow',
        'submit_for_approval',
        'approve',
        'Approve',
        'invoice.transition.approve',
        'Approve the invoice',
        '["Approver", "Admin"]'::jsonb,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    -- submit_for_approval -> create (reject/request changes)
    (
        'InvoiceApprovalWorkflow',
        'submit_for_approval',
        'create',
        'Request Changes',
        'invoice.transition.request_changes',
        'Request changes and return to draft',
        '["Approver", "Admin"]'::jsonb,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    -- approve -> pay
    (
        'InvoiceApprovalWorkflow',
        'approve',
        'pay',
        'Mark as Paid',
        'invoice.transition.pay',
        'Mark invoice as paid',
        '["User", "Admin"]'::jsonb,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    -- pay -> close
    (
        'InvoiceApprovalWorkflow',
        'pay',
        'close',
        'Close Invoice',
        'invoice.transition.close',
        'Close the invoice',
        '["User", "Admin"]'::jsonb,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (workflow_id, from_state, to_state) DO NOTHING;

-- ============================================================================
-- CREATE INVOICE LINE ITEM ENTITY
-- ============================================================================

-- Create InvoiceLineItem entity definition
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
    'InvoiceLineItem',
    'Invoice Line Item',
    'Line items for invoices (master-detail relationship)',
    1,
    1,
    NULL,
    'ENTITY_LEVEL',
    'GLOBAL',
    '{"isDetailEntity": true, "masterEntity": "Invoice"}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100' -- Admin user
) ON CONFLICT (entity_id) DO NOTHING;

-- Create InvoiceLineItem properties
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
    ('InvoiceLineItem', 'description', 'string', 'Description', 'invoiceLineItem.description', TRUE, FALSE, TRUE, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('InvoiceLineItem', 'quantity', 'number', 'Quantity', 'invoiceLineItem.quantity', TRUE, FALSE, TRUE, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('InvoiceLineItem', 'unitPrice', 'number', 'Unit Price', 'invoiceLineItem.unitPrice', TRUE, FALSE, TRUE, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('InvoiceLineItem', 'total', 'calculated', 'Total', 'invoiceLineItem.total', FALSE, TRUE, TRUE, NULL, '{"expression": "unitPrice * quantity"}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (entity_id, property_name) DO NOTHING;

-- ============================================================================
-- CREATE INVOICE ENTITY
-- ============================================================================

-- Create Invoice entity definition
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
    'Invoice',
    'Invoice',
    'Accounts payable invoices',
    1,
    1,
    'InvoiceApprovalWorkflow', -- Link to workflow
    'ENTITY_LEVEL',
    'GLOBAL',
    '{"displayField": "invoiceNumber", "searchDisplay": "invoiceNumber"}'::jsonb,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100' -- Admin user
) ON CONFLICT (entity_id) DO NOTHING;

-- Create Invoice properties
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
    ('Invoice', 'invoiceNumber', 'string', 'Invoice Number', 'invoice.invoiceNumber', TRUE, FALSE, TRUE, NULL, '{"indexable": true}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Invoice', 'vendor', 'reference', 'Vendor', 'invoice.vendor', TRUE, FALSE, TRUE, 'Customer', '{"indexable": true, "displayField": "companyName"}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Invoice', 'invoiceDate', 'date', 'Invoice Date', 'invoice.invoiceDate', TRUE, FALSE, TRUE, NULL, '{"indexable": true}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Invoice', 'dueDate', 'date', 'Due Date', 'invoice.dueDate', TRUE, FALSE, TRUE, NULL, '{"indexable": true}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Invoice', 'amount', 'number', 'Total Amount', 'invoice.amount', TRUE, FALSE, TRUE, NULL, '{"indexable": true}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Invoice', 'lineItems', 'string', 'Line Items', 'invoice.lineItems', FALSE, FALSE, FALSE, NULL, '{"isDetailEntityArray": true, "detailEntityId": "InvoiceLineItem", "minItems": 1}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Invoice', 'description', 'string', 'Description', 'invoice.description', FALSE, FALSE, FALSE, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Invoice', 'notes', 'string', 'Notes', 'invoice.notes', FALSE, FALSE, FALSE, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (entity_id, property_name) DO NOTHING;

-- ============================================================================
-- CREATE SAMPLE INVOICE RECORDS
-- ============================================================================

-- Invoice 1: Office Supplies (Draft)
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
    'd0000000-0000-0000-0000-000000000001'::UUID,
    'Invoice',
    '{
        "invoiceNumber": "INV-2025-001",
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
    'create', -- Initial state
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100'::UUID
) ON CONFLICT DO NOTHING;

-- Invoice 2: IT Services (Pending Approval)
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
    'd0000000-0000-0000-0000-000000000002'::UUID,
    'Invoice',
    '{
        "invoiceNumber": "INV-2025-002",
        "vendor": "b0000000-0000-0000-0000-000000000002",
        "invoiceDate": "2025-01-20",
        "dueDate": "2025-02-20",
        "amount": 3500.00,
        "description": "IT consulting services",
        "lineItems": [
            {"description": "System integration", "quantity": 20, "unitPrice": 150.00, "total": 3000.00},
            {"description": "Training sessions", "quantity": 5, "unitPrice": 100.00, "total": 500.00}
        ]
    }'::jsonb,
    1,
    'submit_for_approval', -- Submitted for approval
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100'::UUID
) ON CONFLICT DO NOTHING;

-- Invoice 3: Marketing Materials (Approved)
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
    'd0000000-0000-0000-0000-000000000003'::UUID,
    'Invoice',
    '{
        "invoiceNumber": "INV-2025-003",
        "vendor": "b0000000-0000-0000-0000-000000000003",
        "invoiceDate": "2025-01-10",
        "dueDate": "2025-02-10",
        "amount": 850.00,
        "description": "Marketing materials and printing",
        "lineItems": [
            {"description": "Brochures printing", "quantity": 1000, "unitPrice": 0.50, "total": 500.00},
            {"description": "Business cards", "quantity": 500, "unitPrice": 0.70, "total": 350.00}
        ]
    }'::jsonb,
    1,
    'approve', -- Approved
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100'::UUID
) ON CONFLICT DO NOTHING;

-- Invoice 4: Software License (Paid)
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
    'd0000000-0000-0000-0000-000000000004'::UUID,
    'Invoice',
    '{
        "invoiceNumber": "INV-2025-004",
        "vendor": "b0000000-0000-0000-0000-000000000004",
        "invoiceDate": "2025-01-05",
        "dueDate": "2025-02-05",
        "amount": 2400.00,
        "description": "Annual software license",
        "lineItems": [
            {"description": "Enterprise license", "quantity": 1, "unitPrice": 2400.00, "total": 2400.00}
        ]
    }'::jsonb,
    1,
    'pay', -- Paid
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100'::UUID
) ON CONFLICT DO NOTHING;

-- Invoice 5: Maintenance Contract (Closed)
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
    'd0000000-0000-0000-0000-000000000005'::UUID,
    'Invoice',
    '{
        "invoiceNumber": "INV-2024-050",
        "vendor": "b0000000-0000-0000-0000-000000000001",
        "invoiceDate": "2024-12-01",
        "dueDate": "2025-01-01",
        "amount": 1200.00,
        "description": "Monthly maintenance contract",
        "lineItems": [
            {"description": "Monthly maintenance", "quantity": 1, "unitPrice": 1200.00, "total": 1200.00}
        ]
    }'::jsonb,
    1,
    'close', -- Closed
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000100'::UUID
) ON CONFLICT DO NOTHING;
