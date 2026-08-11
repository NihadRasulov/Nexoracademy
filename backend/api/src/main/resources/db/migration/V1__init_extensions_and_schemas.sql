-- V1__init_extensions_and_schemas.sql
-- Genişlənmələr və bütün modul sxemlərinin (schema) yaradılması.
-- application.yml-dəki spring.flyway.schemas siyahısı ilə tam üst-üstə düşür.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";   -- gen_random_uuid() üçün
CREATE EXTENSION IF NOT EXISTS "citext";     -- case-insensitive email üçün
CREATE EXTENSION IF NOT EXISTS "vector";     -- kb_articles.embedding üçün (pgvector)

CREATE SCHEMA IF NOT EXISTS identity;    -- users, oauth_accounts, sessions
CREATE SCHEMA IF NOT EXISTS catalog;     -- categories, courses, instructors
CREATE SCHEMA IF NOT EXISTS academics;   -- course_groups, enrollments
CREATE SCHEMA IF NOT EXISTS billing;     -- payments, scholarships
CREATE SCHEMA IF NOT EXISTS outcomes;    -- course_reviews, graduate_outcomes
CREATE SCHEMA IF NOT EXISTS crm;         -- leads, contact_submissions, chat_sessions, campaigns
CREATE SCHEMA IF NOT EXISTS cms;         -- cms_content
CREATE SCHEMA IF NOT EXISTS ai;          -- kb_articles
CREATE SCHEMA IF NOT EXISTS notify;      -- notifications
CREATE SCHEMA IF NOT EXISTS platform;    -- paylaşılan ENUM tipləri + audit_logs
CREATE SCHEMA IF NOT EXISTS analytics;   -- hazırda cədvəl yoxdur, gələcək hesabatlar üçün ayrılıb

-- QEYD: bu extension-ları yaratmaq üçün DB istifadəçisinin superuser
-- olması, ya da CREATE əmrinə icazəsi olması lazımdır. Adətən bunu
-- Postgres admin (məs. Docker-dəki "postgres" superuser) ilə bir dəfəlik
-- edib, sonra tətbiq rolunu (nexora_app) məhdud səlahiyyətlə saxlamaq tövsiyə olunur.