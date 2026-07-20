CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email               VARCHAR(255) NOT NULL,

    status              VARCHAR(50) NOT NULL,

    email_verified_at   TIMESTAMPTZ,

    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email
    ON users(email);