-- =====================================================================
-- V4__academics_schema.sql
-- academics — Cohorts, scheduling, enrollment, student dashboard (Modul 6)
-- =====================================================================

CREATE TYPE academics.group_status AS ENUM ('planned','open','full','in_progress','completed','cancelled');
CREATE TYPE academics.enrollment_status AS ENUM ('waitlisted','pending_payment','confirmed','active','completed','cancelled','refunded');

CREATE TABLE academics.course_groups (
                                         id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         course_id              UUID NOT NULL REFERENCES catalog.courses(id) ON DELETE CASCADE,
                                         group_code             VARCHAR(40) NOT NULL,
                                         start_date             DATE NOT NULL,
                                         end_date               DATE,
                                         registration_deadline  TIMESTAMPTZ,
                                         total_seats            INT NOT NULL CHECK (total_seats >= 0),
                                         reserved_seats         INT NOT NULL DEFAULT 0 CHECK (reserved_seats >= 0),
                                         status                 academics.group_status NOT NULL DEFAULT 'planned',
                                         created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         UNIQUE(course_id, group_code),
                                         CHECK (reserved_seats <= total_seats)
);
CREATE INDEX idx_groups_course_status ON academics.course_groups(course_id, status);

CREATE TABLE academics.schedule_sessions (
                                             id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                             group_id      UUID NOT NULL REFERENCES academics.course_groups(id) ON DELETE CASCADE,
                                             day_of_week   SMALLINT CHECK (day_of_week BETWEEN 0 AND 6),
                                             session_date  DATE,
                                             start_time    TIME NOT NULL,
                                             end_time      TIME NOT NULL,
                                             location_text VARCHAR(255)
);

CREATE TABLE academics.seat_holds (
                                      id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      group_id    UUID NOT NULL REFERENCES academics.course_groups(id) ON DELETE CASCADE,
                                      user_id     UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
                                      held_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                                      expires_at  TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_seat_holds_group ON academics.seat_holds(group_id, expires_at);

CREATE TABLE academics.student_profiles (
                                            user_id       UUID PRIMARY KEY REFERENCES identity.users(id) ON DELETE CASCADE,
                                            date_of_birth DATE,
                                            address       TEXT,
                                            education_bg  TEXT,
                                            referral_code VARCHAR(30),
                                            created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE academics.enrollments (
                                       id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       user_id               UUID NOT NULL REFERENCES identity.users(id),
                                       group_id              UUID NOT NULL REFERENCES academics.course_groups(id),
                                       status                academics.enrollment_status NOT NULL DEFAULT 'pending_payment',
                                       idempotency_key       VARCHAR(100) NOT NULL,
                                       consent_text_version  VARCHAR(20),
                                       consent_given_at      TIMESTAMPTZ,
                                       enrolled_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
                                       completed_at          TIMESTAMPTZ,
                                       cancelled_at          TIMESTAMPTZ,
                                       cancel_reason         VARCHAR(255),
                                       UNIQUE(user_id, group_id),
                                       UNIQUE(idempotency_key)
);
CREATE INDEX idx_enrollments_user ON academics.enrollments(user_id);
CREATE INDEX idx_enrollments_group_status ON academics.enrollments(group_id, status);