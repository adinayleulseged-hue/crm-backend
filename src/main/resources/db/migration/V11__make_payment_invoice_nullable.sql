-- Migration to make invoice_id and customer_id nullable in payments table
-- This allows payments to be recorded even when invoice doesn't exist in database
-- (e.g., frontend has cached/mock invoices)

ALTER TABLE payments 
    ALTER COLUMN invoice_id DROP NOT NULL;

ALTER TABLE payments 
    ALTER COLUMN customer_id DROP NOT NULL;

-- Add comment for documentation
COMMENT ON COLUMN payments.invoice_id IS 'Invoice reference - nullable to support payment recording before invoice sync';
COMMENT ON COLUMN payments.customer_id IS 'Customer reference - nullable, can be derived from invoice when available';
