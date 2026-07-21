-- =====================================================================
-- V2__identity_schema.sql
-- identity — Auth, RBAC, Sessions, OAuth (SRS Modul 4-5)
-- =====================================================================

CREATE TYPE identity.account_status AS ENUM ('pending_verification','active','suspended','deactivated','banned');
CREATE TYPE identity.oauth_provider AS ENUM ('google','github','linkedin');

CREATE TABLE identity.users (
                                id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                email               CITEXT NOT NULL UNIQUE,
                                email_verified_at   TIMESTAMPTZ,
                                phone               VARCHAR(20),
                                full_name           VARCHAR(150) NOT NULL,
                                password_hash       VARCHAR(255),
                                password_algo       VARCHAR(20) DEFAULT 'argon2id',
                                status              identity.account_status NOT NULL DEFAULT 'pending_verification',
                                locale              VARCHAR(10) DEFAULT 'az-AZ',
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
                                         id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                         user_id             UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
                                         provider            identity.oauth_provider NOT NULL,
                                         provider_user_id    VARCHAR(255) NOT NULL,
                                         access_token_enc    TEXT,
                                         refresh_token_enc   TEXT,
                                         linked_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         UNIQUE(provider, provider_user_id)
);
CREATE INDEX idx_oauth_user ON identity.oauth_accounts(user_id);

CREATE TABLE identity.roles (
                                id          SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                code        VARCHAR(40) NOT NULL UNIQUE,
                                name        VARCHAR(80) NOT NULL,
                                description TEXT
);

CREATE TABLE identity.permissions (
                                      id          SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      code        VARCHAR(80) NOT NULL UNIQUE,
                                      description TEXT
);

CREATE TABLE identity.role_permissions (
                                           role_id       SMALLINT NOT NULL REFERENCES identity.roles(id) ON DELETE CASCADE,
                                           permission_id SMALLINT NOT NULL REFERENCES identity.permissions(id) ON DELETE CASCADE,
                                           PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE identity.user_roles (
                                     user_id    UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
                                     role_id    SMALLINT NOT NULL REFERENCES identity.roles(id) ON DELETE CASCADE,
                                     granted_by UUID REFERENCES identity.users(id),
                                     granted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                     PRIMARY KEY (user_id, role_id)
);

CREATE TABLE identity.sessions (
                                   id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   user_id            UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
                                   refresh_token_hash VARCHAR(255) NOT NULL,
                                   ip_address         INET,
                                   user_agent         TEXT,
                                   issued_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
                                   expires_at         TIMESTAMPTZ NOT NULL,
                                   revoked_at         TIMESTAMPTZ
);
CREATE INDEX idx_sessions_user ON identity.sessions(user_id) WHERE revoked_at IS NULL;

CREATE TABLE identity.password_reset_tokens (
                                                id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                user_id     UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
                                                token_hash  VARCHAR(255) NOT NULL,
                                                expires_at  TIMESTAMPTZ NOT NULL,
                                                used_at     TIMESTAMPTZ,
                                                created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE identity.email_verification_tokens (
                                                    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                    user_id     UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
                                                    token_hash  VARCHAR(255) NOT NULL,
                                                    expires_at  TIMESTAMPTZ NOT NULL,
                                                    verified_at TIMESTAMPTZ,
                                                    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE identity.auth_events (
                                      id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      user_id     UUID REFERENCES identity.users(id) ON DELETE SET NULL,
                                      event_type  VARCHAR(40) NOT NULL,
                                      ip_address  INET,
                                      trace_id    UUID NOT NULL DEFAULT gen_random_uuid(),
                                      detail      JSONB,
                                      created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_auth_events_user ON identity.auth_events(user_id, created_at);