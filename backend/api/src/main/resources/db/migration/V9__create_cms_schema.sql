-- V9__create_cms_schema.sql
-- cms sxemi: cms_content
-- (identity.users mövcud olmalıdır)

CREATE TABLE cms.cms_content (
                                 id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 key          VARCHAR(160) NOT NULL UNIQUE,   -- slug (pages/blog) və ya sabit açar (faq-1, social-instagram)
                                 type         platform.cms_content_type NOT NULL,
                                 title        VARCHAR(250),
                                 body         TEXT,
                                 data         JSONB DEFAULT '{}',
    -- page/blog: { "cover_image_url": "...", "author_id": "..." }
    -- faq:       { "sort_order": 1 }
    -- social_link: { "platform": "instagram", "url": "..." }
    -- banner:    { "cta_url": "...", "priority": 1 }
                                 is_published BOOLEAN NOT NULL DEFAULT false,
                                 sort_order   INT NOT NULL DEFAULT 0,
                                 updated_by   UUID REFERENCES identity.users(id),
                                 updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_cms_content_type ON cms.cms_content(type, is_published);