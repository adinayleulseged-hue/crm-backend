-- Insert sample invoices for customer portal testing
-- These invoices will replace the localStorage mock data

-- Invoice 1: INV-LD6YVW (45,000 ETB) - UNPAID
INSERT INTO invoices (
    invoice_number, customer_id, sales_order_id,
    status, invoice_date, due_date,
    subtotal, tax_amount, discount_amount, total_amount, paid_amount, balance_amount,
    currency, notes,
    created_at, updated_at
) VALUES (
    'INV-LD6YVW', 4, NULL,
    'PENDING', '2026-09-01', '2026-10-01',
    45000.00, 0.00, 0.00, 45000.00, 0.00, 45000.00,
    'ETB', 'Enterprise Security Package - Payment Pending',
    NOW(), NOW()
);

-- Invoice 2: INV-KM8PQR (125,000 ETB) - PENDING
INSERT INTO invoices (
    invoice_number, customer_id, sales_order_id,
    status, invoice_date, due_date,
    subtotal, tax_amount, discount_amount, total_amount, paid_amount, balance_amount,
    currency, notes,
    created_at, updated_at
) VALUES (
    'INV-KM8PQR', 4, NULL,
    'PENDING', '2026-08-25', '2026-09-25',
    125000.00, 0.00, 0.00, 125000.00, 0.00, 125000.00,
    'ETB', 'Cyber Defense System Licensing',
    NOW(), NOW()
);

-- Invoice 3: INV-JX4TNS (89000 ETB) - OVERDUE
INSERT INTO invoices (
    invoice_number, customer_id, sales_order_id,
    status, invoice_date, due_date,
    subtotal, tax_amount, discount_amount, total_amount, paid_amount, balance_amount,
    currency, notes,
    created_at, updated_at
) VALUES (
    'INV-JX4TNS', 4, NULL,
    'OVERDUE', '2026-08-10', '2026-09-10',
    89000.00, 0.00, 0.00, 89000.00, 0.00, 89000.00,
    'ETB', 'Network Security Assessment Services',
    NOW(), NOW()
);

-- Invoice 4: INV-WP2CVF (310,000 ETB) - PENDING APPROVAL
INSERT INTO invoices (
    invoice_number, customer_id, sales_order_id,
    status, invoice_date, due_date,
    subtotal, tax_amount, discount_amount, total_amount, paid_amount, balance_amount,
    currency, notes,
    created_at, updated_at
) VALUES (
    'INV-WP2CVF', 4, NULL,
    'DRAFT', '2026-09-03', '2026-10-03',
    310000.00, 0.00, 0.00, 310000.00, 0.00, 310000.00,
    'ETB', 'Annual Cyber Security Maintenance Contract',
    NOW(), NOW()
);

SELECT 'Sample invoices inserted successfully!' as result;
