-- =====================================================================
-- V7__crm_schema.sql
-- crm — Leads, contact/demo forms, syllabus downloads, newsletter (Modul 7,8,9,16)
-- =====================================================================

CREATE TYPE crm.lead_status AS ENUM ('new','contacted','qualified','converted','lost','disqualified');

CREATE TABLE crm.lead_sources (
                                  id   SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  code VARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE crm.leads (
                           id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           full_name             VARCHAR(150),
                           email                 CITEXT,
                           phone                 VARCHAR(20),
                           course_id             UUID REFERENCES catalog.courses(id),
                           source_id             SMALLINT NOT NULL REFERENCES crm.lead_sources(id),
                           status                crm.lead_status NOT NULL DEFAULT 'new',
                           assigned_to           UUID REFERENCES identity.users(id),
                           consent_text_version  VARCHAR(20),
                           consent_given_at      TIMESTAMPTZ,
                           duplicate_of_lead_id  UUID REFERENCES crm.leads(id),
                           created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
                           updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
                           deleted_at            TIMESTAMPTZ
);
CREATE INDEX idx_leads_status_assigned ON crm.leads(status, assigned_to);
CREATE UNIQUE INDEX uq_leads_email_course_open ON crm.leads(email, course_id) WHERE status NOT IN ('lost','disqualified');

CREATE TABLE crm.lead_activities (
                                     id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     lead_id       UUID NOT NULL REFERENCES crm.leads(id) ON DELETE CASCADE,
                                     actor_id      UUID REFERENCES identity.users(id),
                                     activity_type VARCHAR(40) NOT NULL,
                                     notes         TEXT,
                                     created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE crm.contact_submissions (
                                         id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         lead_id      UUID REFERENCES crm.leads(id),
                                         full_name    VARCHAR(150) NOT NULL,
                                         email        CITEXT NOT NULL,
                                         phone        VARCHAR(20),
                                         message      TEXT,
                                         submitted_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE crm.demo_requests (
                                   id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   lead_id        UUID REFERENCES crm.leads(id),
                                   course_id      UUID REFERENCES catalog.courses(id),
                                   preferred_time TIMESTAMPTZ,
                                   status         VARCHAR(20) NOT NULL DEFAULT 'pending',
                                   submitted_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE crm.syllabus_downloads (
                                        id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        lead_id            UUID REFERENCES crm.leads(id),
                                        course_id          UUID NOT NULL REFERENCES catalog.courses(id),
                                        course_version_id  BIGINT REFERENCES catalog.course_versions(id),
                                        downloaded_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE crm.newsletter_subscribers (
                                            id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                            email            CITEXT NOT NULL UNIQUE,
                                            status           VARCHAR(20) NOT NULL DEFAULT 'subscribed',
                                            subscribed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                            unsubscribed_at  TIMESTAMPTZ,
                                            confirm_token    VARCHAR(100)
);