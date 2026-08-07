ALTER TABLE auth_users
    ADD COLUMN IF NOT EXISTS territory_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_auth_users_territory_id ON auth_users (territory_id);
CREATE INDEX IF NOT EXISTS idx_auth_users_role_enabled ON auth_users (role, enabled);
