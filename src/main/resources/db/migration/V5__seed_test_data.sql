-- 1. Insert a Test Tenant
INSERT INTO tenants (id, name, subdomain, is_active)
VALUES ('a0ee93d8-1111-4444-8888-123456789abc', 'Acme Corp', 'acme', true)
ON CONFLICT DO NOTHING;

-- 2. Insert a Test Customer (User)
INSERT INTO users (id, tenant_id, email, password_hash, role)
VALUES ('b1ff04e9-2222-5555-9999-abcdef123456', 'a0ee93d8-1111-4444-8888-123456789abc', 'customer@acme.com', '$2a$10$dummyhash', 'CUSTOMER')
ON CONFLICT DO NOTHING;

-- 3. Insert a Test Product
INSERT INTO products (id, tenant_id, sku, name, description, price)
VALUES ('c2ee05f0-3333-6666-aaaa-987654321fed', 'a0ee93d8-1111-4444-8888-123456789abc', 'SKU-PROD-001', 'Enterprise Server Rack', 'High-performance rack unit', 1200.00)
ON CONFLICT DO NOTHING;

-- 4. Insert Inventory for the Product (10 items in stock)
INSERT INTO inventories (id, tenant_id, product_id, quantity)
VALUES ('d3ff06a1-4444-7777-bbbb-123456abcdef', 'a0ee93d8-1111-4444-8888-123456789abc', 'c2ee05f0-3333-6666-aaaa-987654321fed', 10)
ON CONFLICT DO NOTHING;