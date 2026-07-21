-- =====================================================================
-- V10__notify_schema.sql
-- notify — Notification templates, deliveries, preferences (Modul 27)
-- =====================================================================

CREATE TABLE notify.templates (
                                  id            SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  code          VARCHAR(60) NOT NULL UNIQUE,
                                  channel       VARCHAR(20) NOT NULL,
                                  subject       VARCHAR(200),
                                  body_template TEXT NOT NULL,
                                  locale        VARCHAR(10) DEFAULT 'az-AZ'
);

CREATE TABLE notify.notifications (
                                      id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      user_id     UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
                                      template_id SMALLINT NOT NULL REFERENCES notify.templates(id),
                                      channel     VARCHAR(20) NOT NULL,
                                      payload     JSONB,
                                      status      VARCHAR(20) NOT NULL DEFAULT 'queued',
                                      sent_at     TIMESTAMPTZ,
                                      read_at     TIMESTAMPTZ,
                                      created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notifications_user_status ON notify.notifications(user_id, status);

CREATE TABLE notify.user_preferences (
                                         user_id       UUID PRIMARY KEY REFERENCES identity.users(id) ON DELETE CASCADE,
                                         email_enabled BOOLEAN NOT NULL DEFAULT true,
                                         sms_enabled   BOOLEAN NOT NULL DEFAULT false,
                                         push_enabled  BOOLEAN NOT NULL DEFAULT true
);