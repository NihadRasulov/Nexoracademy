CREATE TABLE roles
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name        VARCHAR(100) NOT NULL,

    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_roles_name UNIQUE(name)
);