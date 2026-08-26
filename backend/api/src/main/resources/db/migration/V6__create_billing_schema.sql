-- V6__create_billing_schema.sql
-- billing sxemi: payments, scholarships
-- (academics.enrollments mövcud olmalıdır)

CREATE TABLE billing.payments (
                                  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  enrollment_id   UUID NOT NULL REFERENCES academics.enrollments(id),
                                  method          platform.payment_method NOT NULL,
                                  amount          NUMERIC(12,2) NOT NULL,
                                  currency        CHAR(3) NOT NULL DEFAULT 'AZN',
                                  status          platform.payment_status NOT NULL DEFAULT 'initiated',
                                  external_txn_id VARCHAR(150),
                                  idempotency_key VARCHAR(100) NOT NULL UNIQUE,
                                  installments    JSONB DEFAULT '[]',
    -- [{ "no": 1, "due_date": "2026-08-01", "amount": 100.00, "status": "captured", "paid_at": "..." }]
                                  refund_amount   NUMERIC(12,2) DEFAULT 0,
                                  refund_reason   VARCHAR(255),
                                  initiated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  captured_at     TIMESTAMPTZ,
                                  failure_reason  VARCHAR(255)
);
CREATE INDEX idx_payments_enrollment ON billing.payments(enrollment_id);

CREATE TABLE billing.scholarships (
                                      id             SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      name           VARCHAR(150) NOT NULL,
                                      description    TEXT,
                                      discount_pct   NUMERIC(5,2) CHECK (discount_pct BETWEEN 0 AND 100),
                                      max_recipients INT,
                                      valid_from     DATE,
                                      valid_until    DATE,
                                      is_active      BOOLEAN NOT NULL DEFAULT true,
                                      applications   JSONB DEFAULT '[]'
    -- [{ "user_id": "...", "enrollment_id": "...", "status": "approved",
    --    "reviewed_by": "...", "reviewed_at": "..." }]
);