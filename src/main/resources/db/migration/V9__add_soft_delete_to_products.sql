-- Migration V9: Add soft delete columns to products table

ALTER TABLE products 
ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Create index for deleted column (for faster queries)
CREATE INDEX IF NOT EXISTS idx_products_deleted ON products(deleted);

-- Comment
COMMENT ON COLUMN products.deleted IS 'Soft delete flag - true means product is deleted from system but kept in database';
COMMENT ON COLUMN products.deleted_at IS 'Timestamp when product was soft deleted';
