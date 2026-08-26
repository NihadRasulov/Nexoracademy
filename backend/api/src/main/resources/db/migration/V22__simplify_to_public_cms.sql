-- Nexora Academy is a public marketing site with one private CMS admin.
-- Keep only catalog, CMS, applications/contact/newsletter and admin authentication.

CREATE TABLE IF NOT EXISTS crm.newsletter_subscriptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           CITEXT NOT NULL UNIQUE,
    consent_version VARCHAR(20) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    subscribed_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO crm.newsletter_subscriptions (id, email, consent_version, active, subscribed_at, updated_at)
SELECT id,
       email,
       COALESCE(consent_text_version, 'legacy'),
       TRUE,
       COALESCE(consent_given_at, created_at, now()),
       COALESCE(updated_at, created_at, now())
FROM crm.leads
WHERE source::text = 'newsletter' AND email IS NOT NULL
ON CONFLICT (email) DO NOTHING;

ALTER TABLE catalog.courses ADD COLUMN IF NOT EXISTS instructor_id UUID;
UPDATE catalog.courses c
SET instructor_id = picked.instructor_id
FROM (
    SELECT DISTINCT ON (course_id) course_id, instructor_id
    FROM catalog.course_instructors
    ORDER BY course_id, CASE WHEN role = 'lead' THEN 0 ELSE 1 END, instructor_id
) picked
WHERE c.id = picked.course_id AND c.instructor_id IS NULL;

ALTER TABLE catalog.courses
    ADD CONSTRAINT fk_courses_instructor
    FOREIGN KEY (instructor_id) REFERENCES catalog.instructors(id) ON DELETE SET NULL;

ALTER TABLE crm.contact_submissions DROP COLUMN IF EXISTS lead_id;
ALTER TABLE catalog.instructors DROP COLUMN IF EXISTS user_id;
DROP TABLE IF EXISTS catalog.course_instructors;

DROP TABLE IF EXISTS crm.chat_sessions;
DROP TABLE IF EXISTS crm.campaigns;
DROP TABLE IF EXISTS crm.leads;
DROP TABLE IF EXISTS identity.oauth_accounts;
DROP TABLE IF EXISTS platform.audit_logs;

DROP SCHEMA IF EXISTS billing CASCADE;
DROP SCHEMA IF EXISTS outcomes CASCADE;
DROP SCHEMA IF EXISTS academics CASCADE;
DROP SCHEMA IF EXISTS ai CASCADE;
DROP SCHEMA IF EXISTS notify CASCADE;

-- Public accounts are no longer a product feature. Preserve authorship only for
-- real admin accounts and remove former student/content/sales identities.
UPDATE catalog.courses
SET created_by = NULL
WHERE created_by IN (
    SELECT id FROM identity.users WHERE role::text NOT IN ('admin', 'system_admin')
);
UPDATE cms.cms_content
SET updated_by = NULL
WHERE updated_by IN (
    SELECT id FROM identity.users WHERE role::text NOT IN ('admin', 'system_admin')
);
DELETE FROM identity.users
WHERE role::text NOT IN ('admin', 'system_admin');

ALTER TABLE identity.users ALTER COLUMN role DROP DEFAULT;
ALTER TYPE platform.user_role RENAME TO user_role_legacy;
CREATE TYPE platform.user_role AS ENUM ('admin');
ALTER TABLE identity.users
    ALTER COLUMN role TYPE platform.user_role
    USING 'admin'::platform.user_role;
ALTER TABLE identity.users ALTER COLUMN role SET DEFAULT 'admin'::platform.user_role;
DROP TYPE platform.user_role_legacy;

ALTER TABLE identity.users ALTER COLUMN status DROP DEFAULT;
ALTER TYPE platform.account_status RENAME TO account_status_legacy;
CREATE TYPE platform.account_status AS ENUM ('active', 'deactivated');
ALTER TABLE identity.users
    ALTER COLUMN status TYPE platform.account_status
    USING (CASE WHEN status::text = 'active' THEN 'active' ELSE 'deactivated' END)::platform.account_status;
ALTER TABLE identity.users ALTER COLUMN status SET DEFAULT 'active'::platform.account_status;
DROP TYPE platform.account_status_legacy;

DELETE FROM identity.sessions WHERE type::text <> 'session';
ALTER TABLE identity.sessions ALTER COLUMN type DROP DEFAULT;
ALTER TYPE platform.session_type RENAME TO session_type_legacy;
CREATE TYPE platform.session_type AS ENUM ('session');
ALTER TABLE identity.sessions
    ALTER COLUMN type TYPE platform.session_type
    USING 'session'::platform.session_type;
ALTER TABLE identity.sessions ALTER COLUMN type SET DEFAULT 'session'::platform.session_type;
DROP TYPE platform.session_type_legacy;

DELETE FROM cms.cms_content
WHERE type::text NOT IN ('page', 'faq', 'news', 'vacancy');
ALTER TABLE cms.cms_content ALTER COLUMN type DROP DEFAULT;
ALTER TYPE platform.cms_content_type RENAME TO cms_content_type_legacy;
CREATE TYPE platform.cms_content_type AS ENUM ('page', 'faq', 'news', 'vacancy');
ALTER TABLE cms.cms_content
    ALTER COLUMN type TYPE platform.cms_content_type
    USING type::text::platform.cms_content_type;
DROP TYPE platform.cms_content_type_legacy;

UPDATE crm.contact_submissions SET type = 'contact'::platform.submission_type;
DROP INDEX IF EXISTS crm.uq_newsletter_email;
DROP INDEX IF EXISTS crm.idx_contact_submissions_type;
ALTER TABLE crm.contact_submissions ALTER COLUMN type DROP DEFAULT;
ALTER TYPE platform.submission_type RENAME TO submission_type_legacy;
CREATE TYPE platform.submission_type AS ENUM ('contact');
ALTER TABLE crm.contact_submissions
    ALTER COLUMN type TYPE platform.submission_type
    USING 'contact'::platform.submission_type;
DROP TYPE platform.submission_type_legacy;

ALTER TABLE identity.users
    DROP COLUMN IF EXISTS email_verified_at,
    DROP COLUMN IF EXISTS failed_login_count,
    DROP COLUMN IF EXISTS locked_until;
ALTER TABLE identity.sessions DROP COLUMN IF EXISTS attempts;

DROP TYPE IF EXISTS platform.oauth_provider;
DROP TYPE IF EXISTS platform.group_status;
DROP TYPE IF EXISTS platform.enrollment_status;
DROP TYPE IF EXISTS platform.payment_method;
DROP TYPE IF EXISTS platform.payment_status;
DROP TYPE IF EXISTS platform.lead_source;
DROP TYPE IF EXISTS platform.lead_status;
DROP TYPE IF EXISTS platform.notification_channel;
DROP TYPE IF EXISTS platform.notification_status;

CREATE INDEX IF NOT EXISTS idx_newsletter_subscriptions_active
    ON crm.newsletter_subscriptions(active, subscribed_at DESC);
CREATE INDEX IF NOT EXISTS idx_applications_status_created
    ON platform.applications(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_contact_submissions_status_created
    ON crm.contact_submissions(status, submitted_at DESC);

INSERT INTO cms.cms_content (key, type, title, body, data, is_published, sort_order)
VALUES
    ('page.about', 'page', 'Nexora Academy haqqında',
     'Nexora Academy texnologiya təhsilini praktik və aydın formada təqdim edir.',
     '{"heroImage":"","stats":{"graduates":0,"employmentRate":0,"instructors":0}}'::jsonb,
     TRUE, 10),
    ('page.contact', 'page', 'Əlaqə', NULL,
     '{"phone":"","email":"","address":"","workingHours":""}'::jsonb,
     TRUE, 20)
ON CONFLICT (key) DO NOTHING;
