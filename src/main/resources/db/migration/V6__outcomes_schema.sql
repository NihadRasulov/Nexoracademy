-- =====================================================================
-- V6__outcomes_schema.sql
-- outcomes — Reviews, graduates, employer partners (Modul 11-13)
-- =====================================================================

CREATE TABLE outcomes.course_reviews (
                                         id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                         course_id     UUID NOT NULL REFERENCES catalog.courses(id) ON DELETE CASCADE,
                                         user_id       UUID NOT NULL REFERENCES identity.users(id),
                                         enrollment_id UUID REFERENCES academics.enrollments(id),
                                         rating        SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                                         comment       TEXT,
                                         is_published  BOOLEAN NOT NULL DEFAULT false,
                                         moderated_by  UUID REFERENCES identity.users(id),
                                         created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         deleted_at    TIMESTAMPTZ
);
CREATE INDEX idx_reviews_course ON outcomes.course_reviews(course_id) WHERE is_published = true;

-- V3-də yaradılan catalog.instructor_ratings.course_review_id sütununa FK əlavə edilir
ALTER TABLE catalog.instructor_ratings
    ADD CONSTRAINT fk_instructor_ratings_review
        FOREIGN KEY (course_review_id) REFERENCES outcomes.course_reviews(id) ON DELETE SET NULL;

CREATE TABLE outcomes.review_sentiment_analyses (
                                                    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                    review_id       BIGINT NOT NULL UNIQUE REFERENCES outcomes.course_reviews(id) ON DELETE CASCADE,
                                                    sentiment_label VARCHAR(20) NOT NULL,
                                                    sentiment_score NUMERIC(4,3),
                                                    key_topics      JSONB,
                                                    model_version   VARCHAR(40),
                                                    analyzed_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE outcomes.partner_companies (
                                            id       SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                            name     VARCHAR(150) NOT NULL,
                                            logo_url TEXT,
                                            website  TEXT
);

CREATE TABLE outcomes.graduate_outcomes (
                                            id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                            user_id             UUID NOT NULL REFERENCES identity.users(id),
                                            course_id           UUID NOT NULL REFERENCES catalog.courses(id),
                                            partner_company_id  SMALLINT REFERENCES outcomes.partner_companies(id),
                                            job_title           VARCHAR(150),
                                            employed_at         DATE,
                                            salary_band         VARCHAR(50),
                                            is_public_story     BOOLEAN NOT NULL DEFAULT false,
                                            story_text          TEXT,
                                            created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);