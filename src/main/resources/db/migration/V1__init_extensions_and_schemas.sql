-- =====================================================================
-- V1__init_extensions_and_schemas.sql
-- Nexora Academy — Initial setup: extensions + 11 subject-area schemas
-- =====================================================================

-- gen_random_uuid() üçün
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- case-insensitive email/username sahələri üçün (identity.users.email, crm.leads.email və s.)
CREATE EXTENSION IF NOT EXISTS "citext";

-- ai.kb_embeddings.embedding (RAG) üçün — server bunu dəstəkləmirsə bu sətri şərh halına sal
-- (Not: pgvector serverdə quraşdırılmalıdır: apt/yum paketi və ya managed Postgres addon)
CREATE EXTENSION IF NOT EXISTS "vector";

-- Subject-area schema-lar (SRS Modul 1-31)
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS catalog;
CREATE SCHEMA IF NOT EXISTS academics;
CREATE SCHEMA IF NOT EXISTS billing;
CREATE SCHEMA IF NOT EXISTS outcomes;
CREATE SCHEMA IF NOT EXISTS crm;
CREATE SCHEMA IF NOT EXISTS cms;
CREATE SCHEMA IF NOT EXISTS ai;
CREATE SCHEMA IF NOT EXISTS notify;
CREATE SCHEMA IF NOT EXISTS platform;
CREATE SCHEMA IF NOT EXISTS analytics;