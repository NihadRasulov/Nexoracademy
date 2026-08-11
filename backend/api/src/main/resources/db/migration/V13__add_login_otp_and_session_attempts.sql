-- Email-based OTP login (2FA-style) and email-based OTP registration verification.
-- 'email_verify' Session rows now store a hashed 6-digit OTP instead of a link token;
-- 'login_otp' is a new Session type for the post-password login step.
ALTER TYPE platform.session_type ADD VALUE IF NOT EXISTS 'login_otp';

-- Brute-force guard for short numeric OTP codes (6 digits = far less entropy than the
-- 256-bit link tokens used elsewhere) — failed guesses increment this, and the OTP is
-- revoked once az.demo.NexoraAcademy.config.AuthProperties#otpMaxAttempts is exceeded.
ALTER TABLE identity.sessions ADD COLUMN attempts SMALLINT NOT NULL DEFAULT 0;
