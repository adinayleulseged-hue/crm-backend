-- Migration V8: Create Products and System Notifications tables

-- =====================================================
-- Table: products
-- =====================================================
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    product_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL CHECK (category IN (
        'SECURITY_HARDWARE',
        'SOFTWARE_LICENSE',
        'CYBERSECURITY_AUDIT',
        'ENTERPRISE_ERP',
        'TRAINING_AND_CERTIFICATION',
        'MAINTENANCE_AND_SUPPORT',
        'OTHER'
    )),
    description TEXT,
    unit_price DECIMAL(15, 2) NOT NULL CHECK (unit_price > 0),
    sku VARCHAR(50) UNIQUE,
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN (
        'ACTIVE',
        'INACTIVE',
        'OUT_OF_STOCK',
        'DISCONTINUED'
    )),
    reorder_level INTEGER,
    supplier_name VARCHAR(255),
    supplier_contact VARCHAR(100),
    warranty_months INTEGER,
    tags VARCHAR(500),
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Indexes for products
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_stock ON products(stock_quantity);

-- =====================================================
-- Table: system_notifications
-- =====================================================
CREATE TABLE IF NOT EXISTS system_notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_username VARCHAR(100),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'INFO' CHECK (type IN (
        'INFO',
        'SUCCESS',
        'WARNING',
        'ERROR',
        'LEAD_CREATED',
        'ORDER_APPROVAL',
        'INVOICE_PAID',
        'SYSTEM_ALERT',
        'TASK_ASSIGNED',
        'DEAL_WON',
        'DEAL_LOST',
        'PAYMENT_RECEIVED',
        'CUSTOMER_INQUIRY'
    )),
    read_status BOOLEAN NOT NULL DEFAULT FALSE,
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    action_url VARCHAR(500),
    priority VARCHAR(20) DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    expires_at TIMESTAMP
);

-- Indexes for system_notifications
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_read ON system_notifications(recipient_username, read_status);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON system_notifications(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON system_notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_related_entity ON system_notifications(related_entity_type, related_entity_id);

-- =====================================================
-- Insert sample products
-- =====================================================
INSERT INTO products (product_code, name, category, description, unit_price, sku, stock_quantity, status, reorder_level, warranty_months)
VALUES 
    ('PRD-2025-001', 'INSA Enterprise Shield Firewall', 'SECURITY_HARDWARE', 
     'High-performance next-generation hardware security appliance with intrusion prevention.',
     450000.00, 'SKU-HW-101', 15, 'ACTIVE', 5, 36),
    
    ('PRD-2025-002', 'Biometric Multi-Factor Terminal', 'SECURITY_HARDWARE',
     'Advanced facial recognition & fingerprint access control hardware unit.',
     120000.00, 'SKU-HW-102', 30, 'ACTIVE', 10, 24),
    
    ('PRD-2025-003', 'INSA ERP Software License (Annual)', 'ENTERPRISE_ERP',
     'Comprehensive Enterprise Resource Planning software subscription for enterprise workflows.',
     850000.00, 'SKU-SW-201', 100, 'ACTIVE', 20, 12),
    
    ('PRD-2025-004', 'Cybersecurity Vulnerability Audit Package', 'CYBERSECURITY_AUDIT',
     'Full organizational penetration testing and infrastructure security assessment.',
     320000.00, 'SKU-SV-301', 50, 'ACTIVE', 15, NULL),
    
    ('PRD-2025-005', 'Network Infrastructure Upgrade Service', 'MAINTENANCE_AND_SUPPORT',
     'Professional network infrastructure upgrade and optimization service.',
     550000.00, 'SKU-SV-302', 25, 'ACTIVE', 10, 12),
    
    ('PRD-2025-006', 'Security Awareness Training Program', 'TRAINING_AND_CERTIFICATION',
     'Comprehensive cybersecurity awareness training for employees.',
     180000.00, 'SKU-TR-401', 40, 'ACTIVE', 15, NULL)
ON CONFLICT (product_code) DO NOTHING;

-- =====================================================
-- Insert sample notifications
-- =====================================================
INSERT INTO system_notifications (recipient_username, title, message, type, read_status, priority)
VALUES 
    (NULL, 'System Update Scheduled', 
     'The CRM system will be updated on Saturday, 3:00 AM - 5:00 AM. Please save your work.',
     'SYSTEM_ALERT', FALSE, 'HIGH'),
    
    (NULL, 'New Product Launch',
     'INSA Enterprise Shield Firewall is now available in the product catalog.',
     'INFO', FALSE, 'NORMAL')
ON CONFLICT DO NOTHING;

-- =====================================================
-- Comments
-- =====================================================
COMMENT ON TABLE products IS 'Product catalog for CRM system';
COMMENT ON TABLE system_notifications IS 'System-wide and user-specific notifications';

COMMENT ON COLUMN products.product_code IS 'Unique product identifier (e.g., PRD-2025-001)';
COMMENT ON COLUMN products.sku IS 'Stock Keeping Unit - unique identifier for inventory';
COMMENT ON COLUMN products.stock_quantity IS 'Current available stock quantity';
COMMENT ON COLUMN products.reorder_level IS 'Minimum stock level before reordering is needed';

COMMENT ON COLUMN system_notifications.recipient_username IS 'Target user (NULL = broadcast to all users)';
COMMENT ON COLUMN system_notifications.related_entity_type IS 'Entity type this notification relates to (e.g., LEAD, ORDER, INVOICE)';
COMMENT ON COLUMN system_notifications.related_entity_id IS 'ID of the related entity';
COMMENT ON COLUMN system_notifications.action_url IS 'URL to navigate when notification is clicked';
