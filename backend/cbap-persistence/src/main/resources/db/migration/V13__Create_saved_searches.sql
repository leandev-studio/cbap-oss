-- CBAP OSS - Create Saved Searches Table
-- Allows users to save search queries and filters for reuse

CREATE TABLE IF NOT EXISTS cbap_saved_searches (
    search_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    entity_id VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    query_text TEXT,
    filters_json JSONB,
    is_global BOOLEAN NOT NULL DEFAULT FALSE, -- If true, search across all entities
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_saved_search_user FOREIGN KEY (user_id) REFERENCES cbap_users(user_id) ON DELETE CASCADE
);

-- Index for fast lookup by user
CREATE INDEX IF NOT EXISTS idx_saved_searches_user_id ON cbap_saved_searches(user_id);
CREATE INDEX IF NOT EXISTS idx_saved_searches_entity_id ON cbap_saved_searches(entity_id);

-- Add comment
COMMENT ON TABLE cbap_saved_searches IS 'User-saved search queries and filters for quick access';
