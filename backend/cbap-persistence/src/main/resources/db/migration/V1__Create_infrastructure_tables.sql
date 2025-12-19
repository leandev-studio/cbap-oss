-- CBAP OSS - Infrastructure Tables Migration
-- Creates tables for authentication, authorization, and system management

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- USERS TABLE
-- ============================================================================
CREATE TABLE cbap_users (
    user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED')),
    tenant_id UUID,
    facility_id UUID,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT fk_user_created_by FOREIGN KEY (created_by) REFERENCES cbap_users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_users_username ON cbap_users(username);
CREATE INDEX idx_users_tenant_id ON cbap_users(tenant_id);
CREATE INDEX idx_users_status ON cbap_users(status);
CREATE INDEX idx_users_facility_id ON cbap_users(facility_id);

-- ============================================================================
-- ROLES TABLE
-- ============================================================================
CREATE TABLE cbap_roles (
    role_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    role_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    tenant_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_roles_role_name ON cbap_roles(role_name);
CREATE INDEX idx_roles_tenant_id ON cbap_roles(tenant_id);

-- ============================================================================
-- USER-ROLE MAPPING TABLE
-- ============================================================================
CREATE TABLE cbap_user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES cbap_users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES cbap_roles(role_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_assigned_by FOREIGN KEY (assigned_by) REFERENCES cbap_users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_user_roles_user_id ON cbap_user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON cbap_user_roles(role_id);

-- ============================================================================
-- PERMISSIONS TABLE
-- ============================================================================
CREATE TABLE cbap_permissions (
    permission_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    permission_name VARCHAR(255) NOT NULL UNIQUE,
    resource_type VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_permissions_permission_name ON cbap_permissions(permission_name);
CREATE INDEX idx_permissions_resource_type ON cbap_permissions(resource_type);
CREATE INDEX idx_permissions_action ON cbap_permissions(action);

-- ============================================================================
-- ROLE-PERMISSION MAPPING TABLE
-- ============================================================================
CREATE TABLE cbap_role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    granted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES cbap_roles(role_id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES cbap_permissions(permission_id) ON DELETE CASCADE
);

CREATE INDEX idx_role_permissions_role_id ON cbap_role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON cbap_role_permissions(permission_id);

-- ============================================================================
-- PASSWORD RESET TOKENS TABLE
-- ============================================================================
CREATE TABLE cbap_password_reset_tokens (
    token_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES cbap_users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_user_id ON cbap_password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_token_hash ON cbap_password_reset_tokens(token_hash);
CREATE INDEX idx_password_reset_expires_at ON cbap_password_reset_tokens(expires_at);

-- ============================================================================
-- ORGANIZATION UNITS TABLE
-- ============================================================================
CREATE TABLE cbap_org_units (
    org_unit_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    org_unit_type VARCHAR(20) NOT NULL CHECK (org_unit_type IN ('HQ', 'REGION', 'FACILITY')),
    name VARCHAR(255) NOT NULL,
    parent_org_unit_id UUID,
    timezone VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_org_units_parent FOREIGN KEY (parent_org_unit_id) REFERENCES cbap_org_units(org_unit_id) ON DELETE SET NULL
);

CREATE INDEX idx_org_units_tenant_id ON cbap_org_units(tenant_id);
CREATE INDEX idx_org_units_type ON cbap_org_units(org_unit_type);
CREATE INDEX idx_org_units_parent ON cbap_org_units(parent_org_unit_id);
CREATE INDEX idx_org_units_status ON cbap_org_units(status);

-- ============================================================================
-- FUNCTION: Update updated_at timestamp
-- ============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON cbap_users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_roles_updated_at BEFORE UPDATE ON cbap_roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_org_units_updated_at BEFORE UPDATE ON cbap_org_units
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
