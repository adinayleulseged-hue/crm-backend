-- Add first_name, last_name, and password columns to crm_customers table

ALTER TABLE crm_customers
    ADD COLUMN IF NOT EXISTS first_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS password VARCHAR(255);

-- Create index for faster searches on name
CREATE INDEX IF NOT EXISTS idx_crm_customers_first_name ON crm_customers (first_name);
CREATE INDEX IF NOT EXISTS idx_crm_customers_last_name ON crm_customers (last_name);
