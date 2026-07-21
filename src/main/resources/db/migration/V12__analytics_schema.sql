-- =====================================================================
-- V12__analytics_schema.sql
-- analytics — Event tracking, reporting aggregates (Modul 30)
-- =====================================================================

CREATE TABLE analytics.events (
                                  id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  user_id     UUID REFERENCES identity.users(id),
                                  session_id  UUID,
                                  course_id   UUID REFERENCES catalog.courses(id),
                                  event_name  VARCHAR(80) NOT NULL,
                                  properties  JSONB,
                                  occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_events_name_time ON analytics.events(event_name, occurred_at);
CREATE INDEX idx_events_course ON analytics.events(course_id);

-- Dashboard üçün gündəlik ön-aqreqasiya (ETL/materialized view refresh ilə yenilənir)
CREATE MATERIALIZED VIEW analytics.daily_course_funnel AS
SELECT
    course_id,
    date_trunc('day', occurred_at) AS day,
    count(*) FILTER (WHERE event_name = 'course_view')          AS views,
    count(*) FILTER (WHERE event_name = 'checkout_start')       AS checkouts_started,
    count(*) FILTER (WHERE event_name = 'enrollment_confirmed') AS enrollments
FROM analytics.events
GROUP BY course_id, date_trunc('day', occurred_at);