-- V11__create_notify_schema.sql
-- notify sxemi: notifications
-- (identity.users mövcud olmalıdır)

CREATE TABLE notify.notifications (
                                      id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      user_id    UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
                                      type       VARCHAR(60) NOT NULL,     -- enrollment_confirmed, payment_due, group_starting_soon
                                      channel    platform.notification_channel NOT NULL,
                                      payload    JSONB DEFAULT '{}',       -- şablon dəyişənləri + göndərilən mətn
                                      status     platform.notification_status NOT NULL DEFAULT 'queued',
                                      sent_at    TIMESTAMPTZ,
                                      read_at    TIMESTAMPTZ,
                                      created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notifications_user_status ON notify.notifications(user_id, status);