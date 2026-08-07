CREATE TABLE IF NOT EXISTS auth_refresh_tokens (
    id              BIGSERIAL PRIMARY KEY,
    token           VARCHAR(128) NOT NULL UNIQUE,
    user_id         BIGINT NOT NULL REFERENCES auth_users (id) ON DELETE CASCADE,
    expires_at      TIMESTAMP NOT NULL,
    revoked         BOOLEAN NOT NULL DEFAULT FALSE,
    replaced_by     VARCHAR(128),
    created_at      TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_auth_refresh_tokens_user_id ON auth_refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_auth_refresh_tokens_token ON auth_refresh_tokens (token);

CREATE TABLE IF NOT EXISTS auth_token_blacklist (
    jti             VARCHAR(64) PRIMARY KEY,
    expires_at      TIMESTAMP NOT NULL,
    revoked_at      TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_auth_token_blacklist_expires_at ON auth_token_blacklist (expires_at);
