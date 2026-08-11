-- Public collection types consumed by the marketing site.  Existing PAGE
-- entries remain valid for static/legal pages; these types support list views.
ALTER TYPE platform.cms_content_type ADD VALUE IF NOT EXISTS 'news';
ALTER TYPE platform.cms_content_type ADD VALUE IF NOT EXISTS 'project';
ALTER TYPE platform.cms_content_type ADD VALUE IF NOT EXISTS 'vacancy';
