CREATE SCHEMA IF NOT EXISTS platform;

CREATE TABLE platform.applications (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    application_type SMALLINT NOT NULL,
    fullname        VARCHAR(200) NOT NULL,
    email           VARCHAR(254) NOT NULL,
    phone           VARCHAR(30) NOT NULL,
    letter          TEXT NOT NULL,
    cv_filename     VARCHAR(255),
    cv_path         VARCHAR(500),
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_applications_status ON platform.applications(status);
CREATE INDEX idx_applications_email ON platform.applications(email);
