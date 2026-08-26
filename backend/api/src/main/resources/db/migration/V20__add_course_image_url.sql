-- Kurs cover şəkli üçün public URL.
-- Fayllar /app/uploads/courses/ altında saxlanılır, nginx /uploads/ ilə serve edir.
ALTER TABLE catalog.courses
    ADD COLUMN image_url TEXT;

COMMENT ON COLUMN catalog.courses.image_url IS
    'Public URL of the course cover image (stored in /app/uploads/courses/)';
