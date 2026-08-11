-- V18__add_performance_indexes.sql
-- Performance-critical indexes identified by code analysis.

-- === CRITICAL: Token-based auth lookups (every auth request) ===
CREATE INDEX idx_sessions_token_hash ON identity.sessions(token_hash);

-- === CRITICAL: Role + composite user filtering (admin panel) ===
CREATE INDEX idx_users_role_status ON identity.users(role, status) WHERE deleted_at IS NULL;

-- === HIGH: CRM lead assignment lookups (sales rep dashboard) ===
CREATE INDEX idx_leads_assigned_to ON crm.leads(assigned_to) WHERE deleted_at IS NULL;

-- === HIGH: Course review user history ===
CREATE INDEX idx_reviews_user_created ON outcomes.course_reviews(user_id, created_at DESC);

-- === HIGH: Course review all-by-course (not just published) ===
CREATE INDEX idx_reviews_course_all ON outcomes.course_reviews(course_id);

-- === HIGH: Payment status filtering (admin dashboard) ===
CREATE INDEX idx_payments_status ON billing.payments(status);

-- === HIGH: Scholarship active+validity check (public listing) ===
CREATE INDEX idx_scholarships_active_validity
    ON billing.scholarships(is_active, valid_from, valid_until);

-- === MEDIUM-HIGH: OTP session lookup (every login attempt) ===
CREATE INDEX idx_sessions_otp_lookup
    ON identity.sessions(user_id, type, issued_at DESC)
    WHERE revoked_at IS NULL AND used_at IS NULL;

-- === MEDIUM: Audit log time-based queries ===
CREATE INDEX idx_audit_created_at ON platform.audit_logs(created_at DESC);

-- === MEDIUM: Notification user time ordering ===
CREATE INDEX idx_notifications_user_created
    ON notify.notifications(user_id, created_at DESC);

-- === LOW-MEDIUM: FK indexes for remaining foreign keys ===
CREATE INDEX idx_categories_parent ON catalog.categories(parent_id);
CREATE INDEX idx_instructors_user ON catalog.instructors(user_id);
CREATE INDEX idx_contact_submissions_lead ON crm.contact_submissions(lead_id);
CREATE INDEX idx_contact_submissions_course ON crm.contact_submissions(course_id);
CREATE INDEX idx_chat_sessions_lead ON crm.chat_sessions(lead_id);
CREATE INDEX idx_grad_outcomes_user ON outcomes.graduate_outcomes(user_id);
CREATE INDEX idx_applications_created_at ON platform.applications(created_at DESC);
