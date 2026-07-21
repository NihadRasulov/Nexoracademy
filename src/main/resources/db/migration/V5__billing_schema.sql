-- =====================================================================
-- V5__billing_schema.sql
-- billing — Payments, installments, scholarships, campaigns (Modul 14,28,29)
-- =====================================================================

CREATE TYPE billing.payment_status AS ENUM ('initiated','authorized','captured','failed','cancelled','refunded','partially_refunded');
CREATE TYPE billing.payment_method AS ENUM ('card','bank_transfer','installment','scholarship_covered');

CREATE TABLE billing.payment_gateways (
                                          id            SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                          provider_code VARCHAR(40) NOT NULL UNIQUE,
                                          is_enabled    BOOLEAN NOT NULL DEFAULT false,
                                          config_json   JSONB
);

CREATE TABLE billing.merchant_accounts (
                                           id                    SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                           gateway_id            SMALLINT NOT NULL REFERENCES billing.payment_gateways(id),
                                           external_merchant_id  VARCHAR(100),
                                           verification_status   VARCHAR(30) DEFAULT 'pending_external',
                                           last_synced_at        TIMESTAMPTZ
);

CREATE TABLE billing.payments (
                                  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  enrollment_id    UUID NOT NULL REFERENCES academics.enrollments(id),
                                  gateway_id       SMALLINT REFERENCES billing.payment_gateways(id),
                                  method           billing.payment_method NOT NULL,
                                  amount           NUMERIC(12,2) NOT NULL,
                                  currency         CHAR(3) NOT NULL DEFAULT 'AZN',
                                  status           billing.payment_status NOT NULL DEFAULT 'initiated',
                                  external_txn_id  VARCHAR(150),
                                  idempotency_key  VARCHAR(100) NOT NULL UNIQUE,
                                  initiated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  captured_at      TIMESTAMPTZ,
                                  failure_reason   VARCHAR(255)
);
CREATE INDEX idx_payments_enrollment ON billing.payments(enrollment_id);

CREATE TABLE billing.payment_installments (
                                              id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                              payment_id     UUID NOT NULL REFERENCES billing.payments(id) ON DELETE CASCADE,
                                              installment_no SMALLINT NOT NULL,
                                              due_date       DATE NOT NULL,
                                              amount         NUMERIC(12,2) NOT NULL,
                                              status         billing.payment_status NOT NULL DEFAULT 'initiated',
                                              paid_at        TIMESTAMPTZ,
                                              UNIQUE(payment_id, installment_no)
);

CREATE TABLE billing.refunds (
                                 id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 payment_id   UUID NOT NULL REFERENCES billing.payments(id),
                                 amount       NUMERIC(12,2) NOT NULL,
                                 reason       VARCHAR(255),
                                 requested_by UUID REFERENCES identity.users(id),
                                 approved_by  UUID REFERENCES identity.users(id),
                                 status       VARCHAR(20) NOT NULL DEFAULT 'pending',
                                 processed_at TIMESTAMPTZ,
                                 created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE billing.scholarships (
                                      id             SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      name           VARCHAR(150) NOT NULL,
                                      description    TEXT,
                                      discount_pct   NUMERIC(5,2) CHECK (discount_pct BETWEEN 0 AND 100),
                                      max_recipients INT,
                                      valid_from     DATE,
                                      valid_until    DATE,
                                      is_active      BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE billing.scholarship_applications (
                                                  id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                  scholarship_id SMALLINT NOT NULL REFERENCES billing.scholarships(id),
                                                  user_id        UUID NOT NULL REFERENCES identity.users(id),
                                                  enrollment_id  UUID REFERENCES academics.enrollments(id),
                                                  status         VARCHAR(20) NOT NULL DEFAULT 'submitted',
                                                  reviewed_by    UUID REFERENCES identity.users(id),
                                                  reviewed_at    TIMESTAMPTZ,
                                                  created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE billing.campaigns (
                                   id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   name              VARCHAR(150) NOT NULL,
                                   banner_image_url  TEXT,
                                   cta_url           TEXT,
                                   discount_pct      NUMERIC(5,2),
                                   starts_at         TIMESTAMPTZ NOT NULL,
                                   ends_at           TIMESTAMPTZ NOT NULL,
                                   is_active         BOOLEAN NOT NULL DEFAULT true,
                                   priority          INT NOT NULL DEFAULT 0,
                                   CHECK (ends_at > starts_at)
);

CREATE TABLE billing.campaign_courses (
                                          campaign_id UUID NOT NULL REFERENCES billing.campaigns(id) ON DELETE CASCADE,
                                          course_id   UUID NOT NULL REFERENCES catalog.courses(id) ON DELETE CASCADE,
                                          PRIMARY KEY (campaign_id, course_id)
);