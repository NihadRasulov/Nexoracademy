-- =====================================================================
-- V8__cms_schema.sql
-- cms — Pages, banners, blog, testimonials, media assets (Modul 15)
-- =====================================================================

CREATE TABLE cms.pages (
                           id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           slug         VARCHAR(160) NOT NULL UNIQUE,
                           title        VARCHAR(200) NOT NULL,
                           body_html    TEXT,
                           is_published BOOLEAN NOT NULL DEFAULT false,
                           published_at TIMESTAMPTZ,
                           updated_by   UUID REFERENCES identity.users(id),
                           updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE cms.blog_posts (
                                id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                slug            VARCHAR(200) NOT NULL UNIQUE,
                                title           VARCHAR(250) NOT NULL,
                                excerpt         VARCHAR(400),
                                body_html       TEXT,
                                cover_image_url TEXT,
                                author_id       UUID REFERENCES identity.users(id),
                                is_published    BOOLEAN NOT NULL DEFAULT false,
                                published_at    TIMESTAMPTZ,
                                created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE cms.media_assets (
                                  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  url         TEXT NOT NULL,
                                  media_type  VARCHAR(30),
                                  alt_text    VARCHAR(255),
                                  uploaded_by UUID REFERENCES identity.users(id),
                                  uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE cms.faqs (
                          id         SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          question   VARCHAR(300) NOT NULL,
                          answer     TEXT NOT NULL,
                          sort_order INT NOT NULL DEFAULT 0,
                          is_active  BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE cms.social_links (
                                  id        SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  platform  VARCHAR(40) NOT NULL,
                                  url       TEXT NOT NULL,
                                  is_active BOOLEAN NOT NULL DEFAULT true
);