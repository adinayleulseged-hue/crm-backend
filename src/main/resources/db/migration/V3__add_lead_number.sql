ALTER TABLE crm_leads
ADD COLUMN lead_number VARCHAR(30);

UPDATE crm_leads
SET lead_number =
'LED-' ||
EXTRACT(YEAR FROM CURRENT_DATE) ||
'-' ||
LPAD(id::text,6,'0');

ALTER TABLE crm_leads
ALTER COLUMN lead_number SET NOT NULL;

ALTER TABLE crm_leads
ADD CONSTRAINT uk_lead_number UNIQUE (lead_number);
