-- V2__create_enum_types.sql
-- Bütün modullar arasında paylaşılan ENUM tipləri.
-- "platform" sxemində saxlanılır ki, hansı modula aid olduqları
-- ilə bağlı qarışıqlıq yaranmasın (bu tiplər identity, catalog, academics,
-- billing, crm, cms, notify cədvəllərində istifadə olunur).

CREATE TYPE platform.user_role            AS ENUM ('guest','student','sales_crm','content_manager','admin','system_admin');
CREATE TYPE platform.account_status       AS ENUM ('pending_verification','active','suspended','deactivated','banned');
CREATE TYPE platform.oauth_provider       AS ENUM ('google','github','linkedin');
CREATE TYPE platform.session_type         AS ENUM ('session','password_reset','email_verify');
CREATE TYPE platform.difficulty_level     AS ENUM ('beginner','intermediate','advanced');
CREATE TYPE platform.delivery_format      AS ENUM ('online','offline','hybrid');
CREATE TYPE platform.group_status         AS ENUM ('planned','open','full','in_progress','completed','cancelled');
CREATE TYPE platform.enrollment_status    AS ENUM ('waitlisted','held','pending_payment','confirmed','completed','cancelled','refunded');
CREATE TYPE platform.payment_method       AS ENUM ('card','bank_transfer','installment','scholarship_covered');
CREATE TYPE platform.payment_status       AS ENUM ('initiated','authorized','captured','failed','cancelled','refunded','partially_refunded');
CREATE TYPE platform.lead_source          AS ENUM ('contact_form','demo_request','syllabus_download','newsletter','chatbot','referral');
CREATE TYPE platform.lead_status          AS ENUM ('new','contacted','qualified','converted','lost','disqualified');
CREATE TYPE platform.submission_type      AS ENUM ('contact','demo','syllabus_download','newsletter');
CREATE TYPE platform.cms_content_type     AS ENUM ('page','faq','social_link','banner');
CREATE TYPE platform.notification_channel AS ENUM ('email','sms','in_app','push');
CREATE TYPE platform.notification_status  AS ENUM ('queued','sent','failed','read');