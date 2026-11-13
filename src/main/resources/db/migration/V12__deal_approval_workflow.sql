-- Migration V12: Deal Approval Workflow
-- Adds columns to support customer deal request → admin approval → order → payment → deal closure workflow

-- Add workflow status column
ALTER TABLE opportunities 
ADD COLUMN IF NOT EXISTS workflow_status VARCHAR(50) DEFAULT 'DRAFT';

-- Add approval tracking columns
ALTER TABLE opportunities 
ADD COLUMN IF NOT EXISTS approved_by BIGINT,
ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS rejected_by BIGINT,
ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(500);

-- Add order and invoice linking
ALTER TABLE opportunities 
ADD COLUMN IF NOT EXISTS order_id BIGINT,
ADD COLUMN IF NOT EXISTS invoice_id BIGINT;

-- Add closure tracking
ALTER TABLE opportunities 
ADD COLUMN IF NOT EXISTS closed_by BIGINT,
ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS close_notes VARCHAR(1000);

-- Add customer permissions flag
ALTER TABLE opportunities 
ADD COLUMN IF NOT EXISTS customer_can_close BOOLEAN DEFAULT FALSE;

-- Add discount request field
ALTER TABLE opportunities 
ADD COLUMN IF NOT EXISTS discount_requested NUMERIC(5,2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS discount_approved NUMERIC(5,2) DEFAULT 0;

-- Add product details
ALTER TABLE opportunities 
ADD COLUMN IF NOT EXISTS product_id BIGINT,
ADD COLUMN IF NOT EXISTS quantity INTEGER DEFAULT 1,
ADD COLUMN IF NOT EXISTS unit_price NUMERIC(15,2);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_opportunities_workflow_status ON opportunities(workflow_status);
CREATE INDEX IF NOT EXISTS idx_opportunities_approved_by ON opportunities(approved_by);
CREATE INDEX IF NOT EXISTS idx_opportunities_order_id ON opportunities(order_id);
CREATE INDEX IF NOT EXISTS idx_opportunities_invoice_id ON opportunities(invoice_id);
CREATE INDEX IF NOT EXISTS idx_opportunities_closed_by ON opportunities(closed_by);
CREATE INDEX IF NOT EXISTS idx_opportunities_product_id ON opportunities(product_id);

-- Add foreign key constraints
ALTER TABLE opportunities 
ADD CONSTRAINT fk_opportunities_order 
FOREIGN KEY (order_id) REFERENCES sales_orders(id) ON DELETE SET NULL;

ALTER TABLE opportunities 
ADD CONSTRAINT fk_opportunities_invoice 
FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE SET NULL;

ALTER TABLE opportunities 
ADD CONSTRAINT fk_opportunities_product 
FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL;

-- Add check constraint for workflow_status values
ALTER TABLE opportunities 
ADD CONSTRAINT chk_workflow_status 
CHECK (workflow_status IN (
    'DRAFT',                    -- Initial state, not yet submitted
    'PENDING_APPROVAL',         -- Submitted, waiting for admin approval
    'APPROVED',                 -- Admin approved, customer can place order
    'REJECTED',                 -- Admin rejected
    'NEEDS_REVISION',           -- Customer needs to revise and resubmit
    'ORDER_PLACED',             -- Customer placed order
    'INVOICE_GENERATED',        -- Invoice auto-created
    'PAYMENT_SUBMITTED',        -- Customer submitted payment
    'PAYMENT_APPROVED',         -- Finance approved payment
    'QUALIFIED',                -- Deal qualified by admin
    'CLOSED_WON',               -- Deal closed won by admin
    'CLOSED_LOST'               -- Deal closed lost by admin
));

-- Add comments for documentation
COMMENT ON COLUMN opportunities.workflow_status IS 'Deal approval workflow status';
COMMENT ON COLUMN opportunities.approved_by IS 'User ID who approved the deal (Admin or Sales Manager)';
COMMENT ON COLUMN opportunities.approved_at IS 'Timestamp when deal was approved';
COMMENT ON COLUMN opportunities.rejected_by IS 'User ID who rejected the deal';
COMMENT ON COLUMN opportunities.rejected_at IS 'Timestamp when deal was rejected';
COMMENT ON COLUMN opportunities.rejection_reason IS 'Reason for rejection';
COMMENT ON COLUMN opportunities.order_id IS 'Linked sales order ID';
COMMENT ON COLUMN opportunities.invoice_id IS 'Linked invoice ID';
COMMENT ON COLUMN opportunities.closed_by IS 'User ID who closed the deal (Admin or Sales Manager)';
COMMENT ON COLUMN opportunities.closed_at IS 'Timestamp when deal was closed';
COMMENT ON COLUMN opportunities.customer_can_close IS 'Flag indicating if customer can close their own deal (always FALSE for customer-initiated deals)';
COMMENT ON COLUMN opportunities.discount_requested IS 'Discount percentage requested by customer';
COMMENT ON COLUMN opportunities.discount_approved IS 'Discount percentage approved by admin';
COMMENT ON COLUMN opportunities.product_id IS 'Product/solution being purchased';
COMMENT ON COLUMN opportunities.quantity IS 'Quantity of products requested';
COMMENT ON COLUMN opportunities.unit_price IS 'Unit price of product (before discount)';

-- Update existing opportunities to have workflow_status
UPDATE opportunities 
SET workflow_status = CASE 
    WHEN stage = 'CLOSED_WON' THEN 'CLOSED_WON'
    WHEN stage = 'CLOSED_LOST' THEN 'CLOSED_LOST'
    WHEN stage = 'NEGOTIATION' THEN 'APPROVED'
    WHEN stage = 'PROPOSAL' THEN 'PENDING_APPROVAL'
    ELSE 'DRAFT'
END
WHERE workflow_status IS NULL OR workflow_status = 'DRAFT';

-- Success message
SELECT 'Migration V12: Deal approval workflow columns added successfully' AS result;
