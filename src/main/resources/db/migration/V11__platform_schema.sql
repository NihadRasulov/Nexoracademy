-- =====================================================================
-- V11__platform_schema.sql
-- platform — Theming/accessibility, feature flags, audit log (Modul 25,26,31)
-- =====================================================================

CREATE TABLE platform.user_ui_preferences (
                                              user_id        UUID PRIMARY KEY REFERENCES identity.users(id) ON DELETE CASCADE,
                                              theme          VARCHAR(10) NOT NULL DEFAULT 'light',
                                              font_scale     NUMERIC(3,2) DEFAULT 1.0,
                                              reduced_motion BOOLEAN NOT NULL DEFAULT false,
                                              high_contrast  BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE platform.feature_flags (
                                        id          SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        code        VARCHAR(60) NOT NULL UNIQUE,
                                        is_enabled  BOOLEAN NOT NULL DEFAULT false,
                                        description TEXT,
                                        updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Modul 32 scope-dan kənar reyestr (Appendix A.3)
CREATE TABLE platform.scope_exclusions (
                                           id          SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                           fr_code     VARCHAR(20) NOT NULL UNIQUE,
                                           title       VARCHAR(200) NOT NULL,
                                           reason      TEXT,
                                           recorded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE platform.audit_logs (
                                     id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     actor_id     UUID REFERENCES identity.users(id),
                                     action       VARCHAR(80) NOT NULL,
                                     entity_type  VARCHAR(60) NOT NULL,
                                     entity_id    TEXT NOT NULL,
                                     before_state JSONB,
                                     after_state  JSONB,
                                     trace_id     UUID NOT NULL DEFAULT gen_random_uuid(),
                                     ip_address   INET,
                                     created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_entity ON platform.audit_logs(entity_type, entity_id);