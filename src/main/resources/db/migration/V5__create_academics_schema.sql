-- V5__create_academics_schema.sql
-- academics sxemi: course_groups, enrollments
-- (catalog.courses və identity.users mövcud olmalıdır)

CREATE TABLE academics.course_groups (
                                         id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         course_id             UUID NOT NULL REFERENCES catalog.courses(id) ON DELETE CASCADE,
                                         group_code            VARCHAR(40) NOT NULL,
                                         start_date            DATE NOT NULL,
                                         end_date              DATE,
                                         registration_deadline TIMESTAMPTZ,
                                         total_seats           INT NOT NULL CHECK (total_seats >= 0),
                                         reserved_seats        INT NOT NULL DEFAULT 0 CHECK (reserved_seats >= 0),
                                         status                platform.group_status NOT NULL DEFAULT 'planned',
                                         schedule              JSONB DEFAULT '[]',
    -- [{ "day_of_week": 1, "start_time": "18:00", "end_time": "20:00", "location": "..." }]
                                         created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         UNIQUE(course_id, group_code),
                                         CHECK (reserved_seats <= total_seats)
);
CREATE INDEX idx_groups_course_status ON academics.course_groups(course_id, status);

CREATE TABLE academics.enrollments (
                                       id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       user_id              UUID NOT NULL REFERENCES identity.users(id),
                                       group_id             UUID NOT NULL REFERENCES academics.course_groups(id),
                                       status               platform.enrollment_status NOT NULL DEFAULT 'pending_payment',
                                       idempotency_key      VARCHAR(100) NOT NULL,
                                       consent_text_version VARCHAR(20),
                                       consent_given_at     TIMESTAMPTZ,
                                       hold_expires_at      TIMESTAMPTZ,        -- 'held' statusu üçün TTL
                                       enrolled_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
                                       completed_at         TIMESTAMPTZ,
                                       cancelled_at         TIMESTAMPTZ,
                                       cancel_reason        VARCHAR(255),
                                       UNIQUE(user_id, group_id),
                                       UNIQUE(idempotency_key)
);
CREATE INDEX idx_enrollments_user ON academics.enrollments(user_id);
CREATE INDEX idx_enrollments_group_status ON academics.enrollments(group_id, status);