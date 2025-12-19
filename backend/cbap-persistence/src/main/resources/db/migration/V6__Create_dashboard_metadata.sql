-- CBAP OSS - Dashboard Metadata Migration
-- Creates dashboard metadata table for user dashboards and pinned items

-- ============================================================================
-- DASHBOARD METADATA TABLE
-- ============================================================================
CREATE TABLE cbap_dashboards (
    dashboard_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL DEFAULT 'My Dashboard',
    layout_config JSONB, -- Widget layout configuration (grid positions, sizes)
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    tenant_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dashboards_user FOREIGN KEY (user_id) REFERENCES cbap_users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_dashboards_user_default UNIQUE (user_id, is_default) DEFERRABLE INITIALLY DEFERRED
);

CREATE INDEX idx_dashboards_user_id ON cbap_dashboards(user_id);
CREATE INDEX idx_dashboards_tenant_id ON cbap_dashboards(tenant_id);
CREATE INDEX idx_dashboards_is_default ON cbap_dashboards(is_default) WHERE is_default = TRUE;

-- ============================================================================
-- DASHBOARD PINS TABLE
-- ============================================================================
CREATE TABLE cbap_dashboard_pins (
    pin_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    dashboard_id UUID NOT NULL,
    pin_type VARCHAR(50) NOT NULL CHECK (pin_type IN ('SEARCH', 'ENTITY_LIST', 'WIDGET')),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    config_json JSONB NOT NULL, -- Pin-specific configuration (search query, entity filters, widget type, etc.)
    display_order INTEGER NOT NULL DEFAULT 0,
    widget_type VARCHAR(100), -- For widget pins (e.g., 'RECENT_ACTIVITY', 'TASKS_SUMMARY')
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dashboard_pins_dashboard FOREIGN KEY (dashboard_id) REFERENCES cbap_dashboards(dashboard_id) ON DELETE CASCADE
);

CREATE INDEX idx_dashboard_pins_dashboard_id ON cbap_dashboard_pins(dashboard_id);
CREATE INDEX idx_dashboard_pins_pin_type ON cbap_dashboard_pins(pin_type);
CREATE INDEX idx_dashboard_pins_display_order ON cbap_dashboard_pins(dashboard_id, display_order);
CREATE INDEX idx_dashboard_pins_config_json ON cbap_dashboard_pins USING GIN (config_json);

-- ============================================================================
-- TRIGGER: Update updated_at for dashboard tables
-- ============================================================================
CREATE TRIGGER update_dashboards_updated_at BEFORE UPDATE ON cbap_dashboards
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_dashboard_pins_updated_at BEFORE UPDATE ON cbap_dashboard_pins
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
