-- =====================================================================
-- V9__ai_schema.sql
-- ai — Chatbot, RAG knowledge base, recommendations, AI safety (Modul 17-24)
-- =====================================================================

CREATE TABLE ai.intents (
                            id          SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            code        VARCHAR(60) NOT NULL UNIQUE,
                            description TEXT
);

CREATE TABLE ai.chat_sessions (
                                  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  user_id    UUID REFERENCES identity.users(id),
                                  lead_id    UUID REFERENCES crm.leads(id),
                                  started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  ended_at   TIMESTAMPTZ,
                                  channel    VARCHAR(30) DEFAULT 'web_widget'
);

CREATE TABLE ai.chat_messages (
                                  id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  session_id         UUID NOT NULL REFERENCES ai.chat_sessions(id) ON DELETE CASCADE,
                                  sender             VARCHAR(10) NOT NULL CHECK (sender IN ('user','assistant','system')),
                                  message_text       TEXT NOT NULL,
                                  detected_intent_id SMALLINT REFERENCES ai.intents(id),
                                  intent_confidence  NUMERIC(4,3),
                                  flagged_unsafe     BOOLEAN NOT NULL DEFAULT false,
                                  created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_chat_messages_session ON ai.chat_messages(session_id, created_at);

CREATE TABLE ai.kb_articles (
                                id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                source_type   VARCHAR(30) NOT NULL,
                                source_ref_id TEXT,
                                title         VARCHAR(250),
                                content       TEXT NOT NULL,
                                is_active     BOOLEAN NOT NULL DEFAULT true,
                                updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Qeyd: VECTOR tipi 'pgvector' extension-ını tələb edir (V1-də quraşdırılıb).
-- Əgər idarəolunan xarici vector DB (Pinecone/Weaviate) istifadə olunacaqsa,
-- bu sütunu 'external_vector_id TEXT' ilə əvəz edən ayrıca migration yaz.
CREATE TABLE ai.kb_embeddings (
                                  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  kb_article_id UUID NOT NULL REFERENCES ai.kb_articles(id) ON DELETE CASCADE,
                                  chunk_index   INT NOT NULL,
                                  chunk_text    TEXT NOT NULL,
                                  embedding     VECTOR(1536),
                                  model_version VARCHAR(40)
);

CREATE TABLE ai.answer_citations (
                                     id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     chat_message_id  BIGINT NOT NULL REFERENCES ai.chat_messages(id) ON DELETE CASCADE,
                                     kb_article_id    UUID NOT NULL REFERENCES ai.kb_articles(id),
                                     relevance_score  NUMERIC(4,3)
);

CREATE TABLE ai.lead_workflow_runs (
                                       id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                       lead_id      UUID NOT NULL REFERENCES crm.leads(id) ON DELETE CASCADE,
                                       session_id   UUID REFERENCES ai.chat_sessions(id),
                                       action_taken VARCHAR(60) NOT NULL,
                                       outcome      VARCHAR(30),
                                       run_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ai.course_recommendations (
                                           id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                           user_id       UUID REFERENCES identity.users(id),
                                           session_id    UUID REFERENCES ai.chat_sessions(id),
                                           course_id     UUID NOT NULL REFERENCES catalog.courses(id),
                                           rank_position SMALLINT,
                                           reason_code   VARCHAR(60),
                                           generated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ai.safety_incidents (
                                     id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     chat_message_id BIGINT REFERENCES ai.chat_messages(id),
                                     incident_type   VARCHAR(60) NOT NULL,
                                     severity        VARCHAR(20) NOT NULL,
                                     detail          JSONB,
                                     detected_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);