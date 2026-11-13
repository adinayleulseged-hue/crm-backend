-- V10: Add workflow tracking fields for complete sales workflow
-- Opportunity → Quotation → Sales Order → Invoice → Finance Approval → Customer Confirmation → Payment

-- ============================================================
-- OPPORTUNITIES: Add quotation_generated flag
-- ============================================================
ALTER TABLE opportunities
ADD COLUMN IF NOT EXISTS quotation_generated BOOLEAN DEFAULT FALSE;

COMMENT ON COLUMN opportunities.quotation_generated IS 'Tracks if a quotation has been generated from this opportunity';

-- ============================================================
-- QUOTATIONS: Add sales_order_generated flag
-- ============================================================
ALTER TABLE quotations
ADD COLUMN IF NOT EXISTS sales_order_generated BOOLEAN DEFAULT FALSE;

COMMENT ON COLUMN quotations.sales_order_generated IS 'Tracks if a sales order has been generated from this quotation';

-- ============================================================
-- SALES_ORDERS: Add invoice_generated flag
-- ============================================================
ALTER TABLE sales_orders
ADD COLUMN IF NOT EXISTS invoice_generated BOOLEAN DEFAULT FALSE;

COMMENT ON COLUMN sales_orders.invoice_generated IS 'Tracks if an invoice has been generated from this sales order';

-- ============================================================
-- INVOICES: Add approval and payment workflow tracking fields
-- ============================================================
ALTER TABLE invoices
ADD COLUMN IF NOT EXISTS approved_by VARCHAR(100);

ALTER TABLE invoices
ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP;

ALTER TABLE invoices
ADD COLUMN IF NOT EXISTS confirmed_by VARCHAR(100);

ALTER TABLE invoices
ADD COLUMN IF NOT EXISTS confirmed_at TIMESTAMP;

ALTER TABLE invoices
ADD COLUMN IF NOT EXISTS paid_by VARCHAR(100);

ALTER TABLE invoices
ADD COLUMN IF NOT EXISTS paid_at TIMESTAMP;

COMMENT ON COLUMN invoices.approved_by IS 'Username of finance officer who approved the invoice';
COMMENT ON COLUMN invoices.approved_at IS 'Timestamp when invoice was approved by finance';
COMMENT ON COLUMN invoices.confirmed_by IS 'Username of customer who confirmed payment';
COMMENT ON COLUMN invoices.confirmed_at IS 'Timestamp when customer confirmed payment';
COMMENT ON COLUMN invoices.paid_by IS 'Username of finance officer who marked invoice as paid';
COMMENT ON COLUMN invoices.paid_at IS 'Timestamp when invoice was marked as paid';

-- ============================================================
-- Create indexes for better query performance
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_opportunities_quotation_generated 
ON opportunities(quotation_generated);

CREATE INDEX IF NOT EXISTS idx_quotations_sales_order_generated 
ON quotations(sales_order_generated);

CREATE INDEX IF NOT EXISTS idx_sales_orders_invoice_generated 
ON sales_orders(invoice_generated);

CREATE INDEX IF NOT EXISTS idx_invoices_approved_at 
ON invoices(approved_at);

CREATE INDEX IF NOT EXISTS idx_invoices_confirmed_at 
ON invoices(confirmed_at);

CREATE INDEX IF NOT EXISTS idx_invoices_paid_at 
ON invoices(paid_at);
