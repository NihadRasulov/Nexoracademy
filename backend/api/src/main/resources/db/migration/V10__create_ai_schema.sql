-- V10__create_ai_schema.sql
-- ai sxemi: kb_articles (pgvector "vector" extension V1-də quraşdırılıb)

CREATE TABLE ai.kb_articles (
                                id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                source_type   VARCHAR(30) NOT NULL,   -- course, faq, page, policy
                                source_ref_id TEXT,
                                title         VARCHAR(250),
                                content       TEXT NOT NULL,
                                embedding     VECTOR(1536),           -- pgvector; xarici vector DB istifadə olunarsa NULL saxlanıla bilər
                                is_active     BOOLEAN NOT NULL DEFAULT true,
                                updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_kb_articles_embedding ON ai.kb_articles USING ivfflat (embedding vector_cosine_ops);