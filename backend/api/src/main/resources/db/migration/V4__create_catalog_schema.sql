-- V4__create_catalog_schema.sql
-- catalog sxemi: categories, courses, instructors, course_instructors
-- (identity.users mövcud olmalıdır - V3-dən sonra işə düşür)

CREATE TABLE catalog.categories (
                                    id         SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                    slug       VARCHAR(80) NOT NULL UNIQUE,
                                    name       VARCHAR(120) NOT NULL,
                                    parent_id  SMALLINT REFERENCES catalog.categories(id),
                                    sort_order INT NOT NULL DEFAULT 0,
                                    is_active  BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE catalog.courses (
                                 id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 slug               VARCHAR(160) NOT NULL UNIQUE,
                                 category_id        SMALLINT NOT NULL REFERENCES catalog.categories(id),
                                 title              VARCHAR(200) NOT NULL,
                                 short_description  VARCHAR(400),
                                 full_description   TEXT,
                                 target_audience    TEXT,
                                 difficulty         platform.difficulty_level NOT NULL,
                                 duration_weeks     SMALLINT,
                                 delivery_format    platform.delivery_format NOT NULL,
                                 location_text      VARCHAR(255),
                                 base_price         NUMERIC(12,2),
                                 currency           CHAR(3) NOT NULL DEFAULT 'AZN',
                                 price_period       VARCHAR(30),
                                 is_published       BOOLEAN NOT NULL DEFAULT false,
                                 is_active          BOOLEAN NOT NULL DEFAULT true,
                                 is_archived        BOOLEAN NOT NULL DEFAULT false,
                                 valid_from         TIMESTAMPTZ,
                                 valid_until        TIMESTAMPTZ,
                                 content            JSONB NOT NULL DEFAULT '{}',
    -- { "objectives": [...], "prerequisites": [...], "tools": [...],
    --   "media": [{"type":"image","url":"...","alt":"..."}],
    --   "tags": ["react","frontend"] }
                                 related_course_ids UUID[] DEFAULT '{}',
                                 created_by         UUID REFERENCES identity.users(id),
                                 created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 deleted_at         TIMESTAMPTZ
);
CREATE INDEX idx_courses_category ON catalog.courses(category_id);
CREATE INDEX idx_courses_published_active ON catalog.courses(is_published, is_active) WHERE deleted_at IS NULL;
CREATE INDEX idx_courses_content_gin ON catalog.courses USING GIN (content jsonb_path_ops);

CREATE TABLE catalog.instructors (
                                     id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     user_id        UUID REFERENCES identity.users(id),
                                     full_name      VARCHAR(150) NOT NULL,
                                     bio            TEXT,
                                     photo_url      TEXT,
                                     linkedin_url   TEXT,
                                     avg_rating     NUMERIC(3,2) DEFAULT 0,
                                     certifications JSONB DEFAULT '[]',
    -- [{ "title": "...", "issuer": "...", "issued_on": "2024-01-01", "credential_url": "..." }]
                                     is_active      BOOLEAN NOT NULL DEFAULT true,
                                     created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE catalog.course_instructors (
                                            course_id     UUID NOT NULL REFERENCES catalog.courses(id) ON DELETE CASCADE,
                                            instructor_id UUID NOT NULL REFERENCES catalog.instructors(id) ON DELETE CASCADE,
                                            role          VARCHAR(40) NOT NULL DEFAULT 'lead',   -- lead, co-instructor, mentor
                                            PRIMARY KEY (course_id, instructor_id)
);