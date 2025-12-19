-- CBAP OSS - Navigation Metadata Migration
-- Creates navigation metadata table for role-aware navigation structure

-- ============================================================================
-- NAVIGATION METADATA TABLE
-- ============================================================================
CREATE TABLE cbap_navigation_items (
    navigation_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    parent_navigation_id UUID,
    label VARCHAR(255) NOT NULL,
    label_key VARCHAR(255), -- i18n key for localization
    icon VARCHAR(100), -- Icon identifier (e.g., "Dashboard", "Settings")
    route_path VARCHAR(500), -- Frontend route path (e.g., "/dashboard", "/entities/users")
    display_order INTEGER NOT NULL DEFAULT 0,
    section VARCHAR(100), -- Optional section grouping (e.g., "Main", "Administration")
    required_permission VARCHAR(255), -- Permission required to see this item (e.g., "ENTITY_READ:User")
    required_role VARCHAR(255), -- Role required to see this item (e.g., "Admin", "User")
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    metadata_json JSONB, -- Additional metadata (badges, tooltips, etc.)
    tenant_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    CONSTRAINT fk_navigation_items_parent FOREIGN KEY (parent_navigation_id) REFERENCES cbap_navigation_items(navigation_id) ON DELETE CASCADE,
    CONSTRAINT fk_navigation_items_created_by FOREIGN KEY (created_by) REFERENCES cbap_users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_navigation_items_parent ON cbap_navigation_items(parent_navigation_id);
CREATE INDEX idx_navigation_items_display_order ON cbap_navigation_items(display_order);
CREATE INDEX idx_navigation_items_section ON cbap_navigation_items(section);
CREATE INDEX idx_navigation_items_visible ON cbap_navigation_items(visible) WHERE visible = TRUE;
CREATE INDEX idx_navigation_items_tenant_id ON cbap_navigation_items(tenant_id);
CREATE INDEX idx_navigation_items_required_role ON cbap_navigation_items(required_role);
CREATE INDEX idx_navigation_items_required_permission ON cbap_navigation_items(required_permission);
CREATE INDEX idx_navigation_items_metadata_json ON cbap_navigation_items USING GIN (metadata_json);

-- ============================================================================
-- TRIGGER: Update updated_at for navigation items
-- ============================================================================
CREATE TRIGGER update_navigation_items_updated_at BEFORE UPDATE ON cbap_navigation_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
