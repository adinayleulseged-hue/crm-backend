-- V14: Add lead qualification tracking fields
-- This migration adds fields to track when and why leads are qualified or disqualified

ALTER TABLE crm_leads
ADD COLUMN IF NOT EXISTS qualified_by BIGINT,
ADD COLUMN IF NOT EXISTS qualified_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS qualification_reason VARCHAR(1000),
ADD COLUMN IF NOT EXISTS disqualification_reason VARCHAR(1000);

-- Add comment for documentation
COMMENT ON COLUMN crm_leads.qualified_by IS 'User ID who qualified or disqualified the lead';
COMMENT ON COLUMN crm_leads.qualified_at IS 'Timestamp when lead was qualified or disqualified';
COMMENT ON COLUMN crm_leads.qualification_reason IS 'Reason for qualifying the lead';
COMMENT ON COLUMN crm_leads.disqualification_reason IS 'Reason for disqualifying the lead';

-- Create index for faster queries on qualified leads
CREATE INDEX IF NOT EXISTS idx_leads_status_qualified_at ON crm_leads(status, qualified_at);
CREATE INDEX IF NOT EXISTS idx_leads_qualified_by ON crm_leads(qualified_by);
