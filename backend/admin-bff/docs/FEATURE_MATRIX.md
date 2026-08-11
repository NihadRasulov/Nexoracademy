# Feature Matrix

This document separates capabilities verified in this repository from work still required for a production-grade operating environment. “Available” describes code that exists in the React/BFF repository; it does not assert that an external Java API or production infrastructure is already deployed.

## Available capabilities

### Platform and security

- Secret, configuration-driven base path shared by the SPA, BFF API, and development OpenAPI.
- Fake `404` responses for conventional admin routes, root login, and root API discovery paths.
- Cookie-based BFF session with no backend token exposed to browser JavaScript.
- Data Protection encryption for tokens stored in the server-side session cache.
- Login brute-force throttling: configurable fixed window, defaulting to 5 attempts per IP per minute.
- Trusted reverse-proxy allowlist for correct client IP and HTTPS handling.
- Panel-wide authenticated-role policy plus controller-level role checks.
- Secure response headers, HSTS outside Development, HTTPS redirection, and strict cookie settings.
- Transactional login, best-effort upstream logout, local session cleanup, and backend token refresh.
- Central error translation for backend, validation, timeout, session, and malformed-protocol failures.
- Configurable Java API base URL and request timeout with startup validation.
- Same-origin production SPA publishing under the secret route.
- Hashed-asset caching, non-cacheable SPA shell, route-based code splitting, and a global UI error boundary.

### Identity and access

- Email/password login and current-user lookup.
- Safe detection and rejection of OTP-required login responses until a complete OTP verification flow is implemented.
- Logout and server-side session removal.
- Current profile update and password change.
- Role-aware navigation and protected routes for admin, content, and sales/CRM responsibilities.
- User account CRUD for admin roles.
- OAuth account, session, notification, and audit-log administration.

### Academic and content operations

- Category CRUD and hierarchy fields.
- Course CRUD with pagination and search.
- Instructor CRUD.
- Course-to-instructor relationship CRUD.
- Course group CRUD, capacity data, dates, status, and schedule payloads.
- Course review CRUD.
- Graduate outcome/story CRUD.
- Knowledge-base article CRUD.
- CMS content CRUD.

### Sales and finance operations

- Enrollment CRUD and explicit cancellation workflow.
- Payment CRUD and explicit capture workflow.
- Scholarship CRUD.
- Campaign CRUD.
- Lead CRUD.
- Contact submission CRUD.
- Chat session CRUD.

### User experience and developer experience

- Responsive desktop/mobile navigation.
- Light and dark themes.
- Reusable data tables, search, pagination, form dialogs, validation feedback, loading/empty/error states, and destructive-action confirmation.
- Reference pickers for related entities, with manual-ID fallback if a role cannot load reference data.
- Azerbaijani user interface and branded Nexora logo/favicon.
- Strict TypeScript build, Oxlint, centralized API client, reusable resource architecture, EditorConfig, contribution guide, and architecture documentation.
- Optional browser-only demo data mode for interface development.

## Not implemented or not production-ready

### P0 — required before a resilient production launch

- **Shared session storage:** the default `DistributedMemoryCache` is process-local, disappears on restart, and does not support multiple BFF replicas. Configure Redis or another real `IDistributedCache` provider.
- **Persisted Data Protection keys:** the key ring must survive restarts and be shared by every replica; protect it with an appropriate certificate, KMS, or platform mechanism.
- **Production secrets and endpoints:** move the real secret route, Java API URL, allowed hosts, proxy IPs, and any credentials to managed deployment configuration. Never ship the sample path as the production path.
- **Restricted host filtering:** replace `AllowedHosts: "*"` with the actual production hostnames.
- **End-to-end TLS:** the BFF-to-Java API URL must use HTTPS in non-local environments, with valid certificate verification.
- **Automated tests:** there are currently no committed unit, integration, authorization-matrix, browser end-to-end, or accessibility tests.
- **CI/CD gates:** there is no committed pipeline enforcing restore, audit, formatting, lint, build, tests, publish, or deployment approval.
- **Observability:** add structured centralized logs, metrics, traces, health/readiness endpoints for the BFF itself, alerting, and exception tracking. Define retention and redact personal data.
- **Multi-instance brute-force protection:** the built-in limiter is per process. Enforce a shared login limit at the gateway/WAF or through a distributed limiter.
- **Authoritative token validation:** validate backend JWT signature, issuer, audience, lifetime, and algorithm in the BFF, or replace local claim parsing with a trusted backend introspection contract.

### P1 — high-value security and operability work

- **Concurrent refresh coordination:** serialize refresh per session and use a distributed strategy when multiple requests or replicas refresh the same token.
- **Explicit CSRF tokens:** `SameSite=Strict`, same-origin production hosting, and narrow CORS reduce risk, but state-changing endpoints do not yet require an anti-forgery token.
- **MFA completion UI:** the BFF exposes the OTP-required state, but this repository does not contain a complete OTP entry/verification flow.
- **BFF security audit events:** record login success/failure, rate-limit rejection, logout, session expiry, and high-risk admin actions in a tamper-resistant audit destination without logging secrets.
- **Contract synchronization:** generate or verify BFF/TypeScript contracts from a versioned Java OpenAPI specification to detect API drift.
- **Container and orchestration assets:** no Dockerfile, deployment manifest, resource limits, autoscaling policy, or zero-downtime migration/runbook is committed.
- **Backup and disaster recovery:** define recovery objectives and test backups for the Java system of record, cache strategy, configuration, and key material.
- **Dependency automation:** add scheduled NuGet/npm vulnerability scanning and controlled update automation.
- **Security testing:** add SAST, secret scanning, dependency policy, DAST, penetration testing, and an incident-response path.

### P2 — product and quality improvements

- Bulk select/actions, CSV/Excel export, import validation, and long-running job feedback.
- Advanced server-side filters and sorting for every large resource list.
- Saved views, dashboard customization, and richer analytics.
- Full localization infrastructure instead of hard-coded Azerbaijani interface strings.
- Formal WCAG audit, keyboard-only end-to-end tests, and screen-reader verification.
- Unsaved-form navigation warnings and draft recovery for long content forms.
- File/media upload workflow instead of URL-only image fields.
- Real-time notification delivery and unread-state workflows.
- Fine-grained permission claims beyond the current role groups.
- Impersonation/support workflows with explicit approvals and audit trails, if the business requires them.

## Known verification limitation

NuGet vulnerability metadata could not be downloaded in the current restricted environment, so `dotnet build` reports `NU1900`. Compilation succeeds, and npm checks can run locally; CI should repeat both package audits with registry access.

The current npm audit also reports the React Router RSC-mode CSRF advisory. This SPA uses declarative `BrowserRouter` routes and none of React Router's RSC/framework action APIs, so the vulnerable path is not reachable here. Version `7.18.2` is retained because it fixes the broader client-router advisories affecting older releases; continue tracking an upstream RSC fix.
