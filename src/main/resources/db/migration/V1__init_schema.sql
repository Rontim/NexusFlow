-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tenants Table
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    subdomain VARCHAR(50) UNIQUE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Users Table (Multi-tenant aware)
CREATE TABLE users (
   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
   tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
   email VARCHAR(255) NOT NULL,
   password_hash VARCHAR(255) NOT NULL,
   role VARCHAR(50) NOT NULL, -- ADMIN, MERCHANT, CUSTOMER
   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT uk_tenant_email UNIQUE (tenant_id, email)
);

CREATE INDEX idx_users_tenant ON users(tenant_id);