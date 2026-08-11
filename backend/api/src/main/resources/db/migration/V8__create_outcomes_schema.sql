-- V8__create_outcomes_schema.sql
-- outcomes sxemi: course_reviews, graduate_outcomes
-- (catalog.courses, identity.users, academics.enrollments mövcud olmalıdır)

CREATE TABLE outcomes.course_reviews (
                                         id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                         course_id     UUID NOT NULL REFERENCES catalog.courses(id) ON DELETE CASCADE,
                                         user_id       UUID NOT NULL REFERENCES identity.users(id),
                                         enrollment_id UUID REFERENCES academics.enrollments(id),
                                         rating        SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                                         comment       TEXT,
                                         is_published  BOOLEAN NOT NULL DEFAULT false,
                                         moderated_by  UUID REFERENCES identity.users(id),
                                         ai_sentiment  JSONB,
    -- { "label": "positive", "score": 0.82, "topics": ["instructor","pricing"], "model_version": "..." }
                                         created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         deleted_at    TIMESTAMPTZ
);
CREATE INDEX idx_reviews_course ON outcomes.course_reviews(course_id) WHERE is_published = true;

CREATE TABLE outcomes.graduate_outcomes (
                                            id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                            user_id         UUID NOT NULL REFERENCES identity.users(id),
                                            course_id       UUID NOT NULL REFERENCES catalog.courses(id),
                                            company_name    VARCHAR(150),
                                            job_title       VARCHAR(150),
                                            employed_at     DATE,
                                            salary_band     VARCHAR(50),
                                            is_public_story BOOLEAN NOT NULL DEFAULT false,
                                            story_text      TEXT,
                                            created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_grad_outcomes_course ON outcomes.graduate_outcomes(course_id);