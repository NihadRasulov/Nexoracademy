-- V3__create_identity_schema.sql
-- identity sxemi: users, oauth_accounts, sessions

CREATE TABLE identity.users (
                                id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                email               CITEXT NOT NULL UNIQUE,
                                email_verified_at   TIMESTAMPTZ,
                                phone               VARCHAR(20),
                                full_name           VARCHAR(150) NOT NULL,
                                password_hash       VARCHAR(255),               -- NULL => yalnız OAuth
                                role                platform.user_role NOT NULL DEFAULT 'student',
                                status              platform.account_status NOT NULL DEFAULT 'pending_verification',
                                locale              VARCHAR(10) DEFAULT 'az-AZ',
                                profile             JSONB DEFAULT '{}',
    -- { "date_of_birth": "...", "address": "...", "education_bg": "...",
    --   "theme": "light", "font_scale": 1.0, "reduced_motion": false }
                                last_login_at       TIMESTAMPTZ,
                                failed_login_count  SMALLINT NOT NULL DEFAULT 0,
                                locked_until        TIMESTAMPTZ,
                                created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
                                updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
                                deleted_at          TIMESTAMPTZ
);
CREATE INDEX idx_users_status ON identity.users(status) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_users_phone ON identity.users(phone) WHERE phone IS NOT NULL AND deleted_at IS NULL;

CREATE TABLE identity.oauth_accounts (
                                         id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                         user_id           UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
                                         provider          platform.oauth_provider NOT NULL,
                                         provider_user_id  VARCHAR(255) NOT NULL,
                                         access_token_enc  TEXT,
                                         refresh_token_enc TEXT,
                                         linked_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         UNIQUE(provider, provider_user_id)
);
CREATE INDEX idx_oauth_user ON identity.oauth_accounts(user_id);

CREATE TABLE identity.sessions (
                                   id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   user_id      UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
                                   type         platform.session_type NOT NULL DEFAULT 'session',
                                   token_hash   VARCHAR(255) NOT NULL,
                                   ip_address   INET,
                                   user_agent   TEXT,
                                   issued_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                   expires_at   TIMESTAMPTZ NOT NULL,
                                   used_at      TIMESTAMPTZ,
                                   revoked_at   TIMESTAMPTZ
);
CREATE INDEX idx_sessions_user_type ON identity.sessions(user_id, type) WHERE revoked_at IS NULL;