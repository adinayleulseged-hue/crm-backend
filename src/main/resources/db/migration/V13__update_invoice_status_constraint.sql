-- Update invoice status constraint to include APPROVED and CONFIRMED states
-- Drop the old constraint
ALTER TABLE invoices DROP CONSTRAINT IF EXISTS invoices_status_check;

-- Add the new constraint with additional statuses
ALTER TABLE invoices ADD CONSTRAINT invoices_status_check
    CHECK (status IN (
        'DRAFT', 'PENDING', 'SENT', 'APPROVED', 'CONFIRMED',
        'PAID', 'OVERDUE', 'CANCELLED'
    ));

-- Add comment to document the workflow
COMMENT ON COLUMN invoices.status IS 'Workflow: DRAFT → PENDING (awaiting payment) → APPROVED (finance approved, awaiting customer confirmation) → CONFIRMED (customer confirmed payment) → PAID (payment completed)';
