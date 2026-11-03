-- Products Table (Multi-tenant)
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_sku UNIQUE (tenant_id, sku)
);

-- Inventory Table (Optimized for high-concurrency locking)
CREATE TABLE inventories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity INT NOT NULL CHECK (quantity >= 0),
    version BIGINT NOT NULL DEFAULT 0, -- For optimistic locking / version control
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_product_inventory UNIQUE (tenant_id, product_id)
);

CREATE INDEX idx_products_tenant ON products(tenant_id);
CREATE INDEX idx_inventories_tenant_product ON inventories(tenant_id, product_id);