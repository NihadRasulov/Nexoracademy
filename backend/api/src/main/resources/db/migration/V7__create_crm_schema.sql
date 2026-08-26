-- V7__create_crm_schema.sql
-- crm sxemi: leads, contact_submissions, chat_sessions, campaigns
-- (catalog.courses və identity.users mövcud olmalıdır)

CREATE TABLE crm.leads (
                           id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           full_name            VARCHAR(150),
                           email                CITEXT,
                           phone                VARCHAR(20),
                           course_id            UUID REFERENCES catalog.courses(id),
                           source               platform.lead_source NOT NULL,
                           status               platform.lead_status NOT NULL DEFAULT 'new',
                           assigned_to          UUID REFERENCES identity.users(id),
                           consent_text_version VARCHAR(20),
                           consent_given_at     TIMESTAMPTZ,
                           duplicate_of_lead_id UUID REFERENCES crm.leads(id),
                           activity_log         JSONB DEFAULT '[]',
    -- [{ "actor_id": "...", "type": "call", "notes": "...", "created_at": "..." }]
                           created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
                           updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
                           deleted_at           TIMESTAMPTZ
);
CREATE INDEX idx_leads_status_assigned ON crm.leads(status, assigned_to);
CREATE UNIQUE INDEX uq_leads_email_course_open ON crm.leads(email, course_id) WHERE status NOT IN ('lost','disqualified');

CREATE TABLE crm.contact_submissions (
                                         id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         lead_id        UUID REFERENCES crm.leads(id),
                                         type           platform.submission_type NOT NULL,
                                         course_id      UUID REFERENCES catalog.courses(id),
                                         full_name      VARCHAR(150),
                                         email          CITEXT,
                                         phone          VARCHAR(20),
                                         message        TEXT,
                                         preferred_time TIMESTAMPTZ,          -- yalnız type='demo' üçün
                                         status         VARCHAR(20) DEFAULT 'pending',  -- pending, scheduled, completed, no_show (demo üçün)
                                         submitted_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_contact_submissions_type ON crm.contact_submissions(type, submitted_at);
-- newsletter tipli qeydlərdə email unikal olmalıdır (qismən unikal indeks):
CREATE UNIQUE INDEX uq_newsletter_email ON crm.contact_submissions(email) WHERE type = 'newsletter';

CREATE TABLE crm.chat_sessions (
                                   id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   user_id    UUID REFERENCES identity.users(id),     -- qonaq söhbətlərə icazə verilir
                                   lead_id    UUID REFERENCES crm.leads(id),
                                   channel    VARCHAR(30) DEFAULT 'web_widget',
                                   messages   JSONB NOT NULL DEFAULT '[]',
    -- [{ "sender": "user|assistant|system", "text": "...", "intent": "ask_price",
    --    "confidence": 0.91, "flagged_unsafe": false, "citations": ["kb_article_id"], "at": "..." }]
                                   started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                   ended_at   TIMESTAMPTZ
);
CREATE INDEX idx_chat_sessions_user ON crm.chat_sessions(user_id);
CREATE INDEX idx_chat_sessions_messages_gin ON crm.chat_sessions USING GIN (messages jsonb_path_ops);

CREATE TABLE crm.campaigns (
                               id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               name             VARCHAR(150) NOT NULL,
                               banner_image_url TEXT,
                               cta_url          TEXT,
                               discount_pct     NUMERIC(5,2),
                               starts_at        TIMESTAMPTZ NOT NULL,
                               ends_at          TIMESTAMPTZ NOT NULL,
                               is_active        BOOLEAN NOT NULL DEFAULT true,
                               priority         INT NOT NULL DEFAULT 0,
                               course_ids       UUID[] NOT NULL DEFAULT '{}',
                               CHECK (ends_at > starts_at)
);
CREATE INDEX idx_campaigns_course_ids ON crm.campaigns USING GIN (course_ids);