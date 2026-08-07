-- Pricing rules
CREATE TABLE IF NOT EXISTS pricing_rules (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(50)  NOT NULL UNIQUE,
    name                VARCHAR(200) NOT NULL,
    rule_type           VARCHAR(40)  NOT NULL,
    value               NUMERIC(15, 4) NOT NULL,
    min_order_amount    NUMERIC(15, 2),
    max_adjustment      NUMERIC(15, 2),
    product_sku         VARCHAR(100),
    priority            INTEGER NOT NULL DEFAULT 100,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    valid_from          DATE,
    valid_to            DATE,
    description         VARCHAR(1000),
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pricing_rules_active ON pricing_rules (active);
CREATE INDEX IF NOT EXISTS idx_pricing_rules_sku ON pricing_rules (product_sku);

-- Sales territories
CREATE TABLE IF NOT EXISTS territories (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(50)  NOT NULL UNIQUE,
    name                VARCHAR(200) NOT NULL,
    region              VARCHAR(100),
    description         VARCHAR(1000),
    manager_username    VARCHAR(100),
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_territories_active ON territories (active);
CREATE INDEX IF NOT EXISTS idx_territories_region ON territories (region);

-- Campaign metrics snapshots
CREATE TABLE IF NOT EXISTS campaign_metrics (
    id                  BIGSERIAL PRIMARY KEY,
    campaign_id         BIGINT NOT NULL REFERENCES campaigns (id) ON DELETE CASCADE,
    recorded_at         TIMESTAMP NOT NULL,
    status_snapshot     VARCHAR(40),
    budget              NUMERIC(15, 2),
    expected_revenue    NUMERIC(15, 2),
    actual_revenue      NUMERIC(15, 2),
    expected_leads      INTEGER,
    actual_leads        INTEGER,
    notes               VARCHAR(1000)
);

CREATE INDEX IF NOT EXISTS idx_campaign_metrics_campaign_id ON campaign_metrics (campaign_id);
CREATE INDEX IF NOT EXISTS idx_campaign_metrics_recorded_at ON campaign_metrics (recorded_at);

-- Invoice reminder tracking
ALTER TABLE invoices
    ADD COLUMN IF NOT EXISTS last_reminder_sent_at TIMESTAMP;
