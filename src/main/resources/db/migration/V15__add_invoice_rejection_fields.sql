-- V15: Add invoice rejection fields for finance approval workflow
-- Allows finance officer to reject invoice with reason

ALTER TABLE invoices
ADD COLUMN IF NOT EXISTS rejected_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS rejection_reason TEXT;

-- Add comment
COMMENT ON COLUMN invoices.rejected_by IS 'User who rejected the invoice';
COMMENT ON COLUMN invoices.rejected_at IS 'Timestamp when invoice was rejected';
COMMENT ON COLUMN invoices.rejection_reason IS 'Reason for invoice rejection';
