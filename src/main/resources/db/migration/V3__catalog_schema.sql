-- =====================================================================
-- V3__catalog_schema.sql
-- catalog — Public site, course catalog, instructors (SRS Modul 1,2,3,10)
-- =====================================================================

CREATE TYPE catalog.difficulty AS ENUM ('beginner','intermediate','advanced');
CREATE TYPE catalog.delivery_format AS ENUM ('online','offline','hybrid');

CREATE TABLE catalog.categories (
                                    id          SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                    slug        VARCHAR(80) NOT NULL UNIQUE,
                                    name        VARCHAR(120) NOT NULL,
                                    parent_id   SMALLINT REFERENCES catalog.categories(id),
                                    sort_order  INT NOT NULL DEFAULT 0,
                                    is_active   BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE catalog.courses (
                                 id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 slug                VARCHAR(160) NOT NULL UNIQUE,
                                 category_id         SMALLINT NOT NULL REFERENCES catalog.categories(id),
                                 title               VARCHAR(200) NOT NULL,
                                 short_description   VARCHAR(400),
                                 full_description    TEXT,
                                 target_audience     TEXT,
                                 difficulty          catalog.difficulty NOT NULL,
                                 duration_weeks      SMALLINT,
                                 delivery_format     catalog.delivery_format NOT NULL,
                                 location_text       VARCHAR(255),
                                 cover_image_url     TEXT,
                                 base_price          NUMERIC(12,2),
                                 currency            CHAR(3) NOT NULL DEFAULT 'AZN',
                                 price_period        VARCHAR(30),
                                 is_published        BOOLEAN NOT NULL DEFAULT false,
                                 is_active           BOOLEAN NOT NULL DEFAULT true,
                                 is_archived         BOOLEAN NOT NULL DEFAULT false,
                                 valid_from          TIMESTAMPTZ,
                                 valid_until         TIMESTAMPTZ,
                                 created_by          UUID REFERENCES identity.users(id),
                                 created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 deleted_at          TIMESTAMPTZ
);
CREATE INDEX idx_courses_category ON catalog.courses(category_id);
CREATE INDEX idx_courses_published_active ON catalog.courses(is_published, is_active) WHERE deleted_at IS NULL;

CREATE TABLE catalog.course_versions (
                                         id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                         course_id       UUID NOT NULL REFERENCES catalog.courses(id) ON DELETE CASCADE,
                                         version_label   VARCHAR(20) NOT NULL,
                                         change_summary  TEXT,
                                         published_at    TIMESTAMPTZ,
                                         is_current      BOOLEAN NOT NULL DEFAULT false,
                                         created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         UNIQUE(course_id, version_label)
);

CREATE TABLE catalog.course_media (
                                      id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      course_id   UUID NOT NULL REFERENCES catalog.courses(id) ON DELETE CASCADE,
                                      media_type  VARCHAR(20) NOT NULL,
                                      url         TEXT NOT NULL,
                                      alt_text    VARCHAR(255),
                                      sort_order  INT NOT NULL DEFAULT 0
);

CREATE TABLE catalog.course_prerequisites (
                                              id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                              course_id   UUID NOT NULL REFERENCES catalog.courses(id) ON DELETE CASCADE,
                                              description VARCHAR(400) NOT NULL,
                                              sort_order  INT NOT NULL DEFAULT 0
);

CREATE TABLE catalog.course_learning_objectives (
                                                    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                    course_id   UUID NOT NULL REFERENCES catalog.courses(id) ON DELETE CASCADE,
                                                    objective   VARCHAR(400) NOT NULL,
                                                    sort_order  INT NOT NULL DEFAULT 0
);

CREATE TABLE catalog.course_tools (
                                      id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      course_id   UUID NOT NULL REFERENCES catalog.courses(id) ON DELETE CASCADE,
                                      tool_name   VARCHAR(100) NOT NULL,
                                      usage_note  VARCHAR(400)
);

CREATE TABLE catalog.tags (
                              id   SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              name VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE catalog.course_tags (
                                     course_id UUID NOT NULL REFERENCES catalog.courses(id) ON DELETE CASCADE,
                                     tag_id    SMALLINT NOT NULL REFERENCES catalog.tags(id) ON DELETE CASCADE,
                                     PRIMARY KEY (course_id, tag_id)
);

CREATE TABLE catalog.instructors (
                                     id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     user_id       UUID REFERENCES identity.users(id),
                                     full_name     VARCHAR(150) NOT NULL,
                                     bio           TEXT,
                                     photo_url     TEXT,
                                     linkedin_url  TEXT,
                                     avg_rating    NUMERIC(3,2) DEFAULT 0,
                                     is_active     BOOLEAN NOT NULL DEFAULT true,
                                     created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE catalog.instructor_certifications (
                                                   id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                   instructor_id  UUID NOT NULL REFERENCES catalog.instructors(id) ON DELETE CASCADE,
                                                   title          VARCHAR(200) NOT NULL,
                                                   issuer         VARCHAR(150),
                                                   issued_on      DATE,
                                                   credential_url TEXT
);

-- Qeyd: course_review_id-ə FK V6 migration-da (outcomes schema yaradıldıqdan sonra) əlavə olunur
CREATE TABLE catalog.instructor_ratings (
                                            id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                            instructor_id    UUID NOT NULL REFERENCES catalog.instructors(id) ON DELETE CASCADE,
                                            course_review_id BIGINT,
                                            rating           SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                                            created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE catalog.course_instructors (
                                            course_id     UUID NOT NULL REFERENCES catalog.courses(id) ON DELETE CASCADE,
                                            instructor_id UUID NOT NULL REFERENCES catalog.instructors(id) ON DELETE CASCADE,
                                            role          VARCHAR(40) DEFAULT 'lead',
                                            PRIMARY KEY (course_id, instructor_id)
);

CREATE TABLE catalog.course_related (
                                        course_id         UUID NOT NULL REFERENCES catalog.courses(id) ON DELETE CASCADE,
                                        related_course_id UUID NOT NULL REFERENCES catalog.courses(id) ON DELETE CASCADE,
                                        sort_order        INT NOT NULL DEFAULT 0,
                                        PRIMARY KEY (course_id, related_course_id),
                                        CHECK (course_id <> related_course_id)
);