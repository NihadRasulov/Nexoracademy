-- V12__create_platform_schema.sql
-- platform sxemi: audit_logs
-- (identity.users mövcud olmalıdır)

CREATE TABLE platform.audit_logs (
                                     id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     actor_id     UUID REFERENCES identity.users(id),
                                     action       VARCHAR(80) NOT NULL,     -- course.publish, refund.approve, role.grant, login_fail...
                                     entity_type  VARCHAR(60) NOT NULL,
                                     entity_id    TEXT NOT NULL,
                                     before_state JSONB,
                                     after_state  JSONB,
                                     trace_id     UUID NOT NULL DEFAULT gen_random_uuid(),
                                     ip_address   INET,
                                     created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_entity ON platform.audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_actor_time ON platform.audit_logs(actor_id, created_at);