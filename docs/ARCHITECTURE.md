# Architecture

## System context

```text
Browser
  └─ React SPA
       └─ same-origin JSON + HttpOnly session cookie
            └─ ASP.NET Core Admin BFF
                 └─ Bearer access token + JSON
                      └─ Nexora Java REST API
```

The React application never receives a backend access token. It authenticates to the BFF with a secure, scoped, `HttpOnly` cookie. The BFF resolves the server-side session, attaches the access token to its Java API request, refreshes the token when necessary, and maps the response back to a browser-safe contract.

## Repository boundaries

| Area | Responsibility |
|---|---|
| `NexoraAdminPanelUI/src/auth` | Browser authentication state and role-aware route guards |
| `NexoraAdminPanelUI/src/pages` | Screens with domain-specific behavior |
| `NexoraAdminPanelUI/src/resources` | Reusable configuration-driven CRUD screens |
| `NexoraAdminPanelUI/src/components` | Shared, domain-neutral UI building blocks |
| `NexoraAdminPanelUI/src/lib` | HTTP, formatting, demo infrastructure, and helpers |
| `AdminBff/Controllers` | Browser-facing orchestration and authorization boundaries |
| `AdminBff/Clients` | Typed Java API adapters and protocol error mapping |
| `AdminBff/Auth` | Cookie identity, protected server-side sessions, roles, and token forwarding |
| `AdminBff/Configuration` | Validated options and dependency registration |
| `AdminBff/Middleware` | Cross-cutting route concealment, headers, and error responses |
| `AdminBff/Contracts` | Explicit Java and BFF request/response models |
| `AdminBff/Routing` | Secret-prefix SPA and static-file endpoint mapping |

## Request routing

`AdminSettings:SecretPath` is validated during startup and becomes the single base path for the SPA and all BFF controllers. For a value such as `sys-control-9912`:

- SPA login: `/sys-control-9912/login`
- BFF login: `/sys-control-9912/api/auth/login`
- BFF resources: `/sys-control-9912/api/...`
- development OpenAPI: `/sys-control-9912/openapi/v1.json`

Common discovery routes such as `/admin`, `/admin/login`, and `/administrator` deliberately return `404`. Requests to the old root `/api` and `/login` paths are also concealed. This is defense in depth; authorization is still mandatory.

The Vite development base is read from the same BFF configuration. Production publish builds the SPA and includes its assets in the BFF output, so the final deployment is same-origin by default.

## Authentication lifecycle

1. The browser submits credentials to the rate-limited BFF login endpoint.
2. The BFF calls the Java authentication endpoint.
3. The returned token metadata is validated. OTP-only responses are rejected without creating a browser session because the repository does not yet contain the OTP completion flow.
4. Backend tokens are encrypted with ASP.NET Core Data Protection and written to `IDistributedCache` under a random session ID.
5. The BFF verifies identity, active status, and the authoritative panel role against the backend `/me` endpoint before issuing the browser cookie.
6. The cookie contains identity and session claims only; it contains no backend token.
7. Authenticated typed clients use `BackendAuthorizationHandler` to attach and, when necessary, refresh the backend access token.
8. Logout attempts backend revocation and always removes the local session and cookie.

Login completion is transactional: a partial downstream failure does not leave an authenticated browser cookie behind.

## Authorization model

The BFF is the authoritative enforcement point. React guards and filtered navigation improve UX but are not security boundaries.

| Policy group | Roles |
|---|---|
| Panel access | `ADMIN`, `SYSTEM_ADMIN`, `CONTENT_MANAGER`, `SALES_CRM` |
| Admin only | `ADMIN`, `SYSTEM_ADMIN` |
| Content manager | Admin roles plus `CONTENT_MANAGER` |
| Sales/CRM | Admin roles plus `SALES_CRM` |

The secret-prefixed controller group requires the panel-access policy globally. `[AllowAnonymous]` is limited to the login and health endpoints. Resource controllers then apply their narrower role group.

## Security controls

- `HttpOnly`, `SameSite=Strict`, secret-path-scoped cookie; `Secure` is mandatory outside Development.
- Login fixed-window rate limit by effective client IP.
- Explicit trusted-proxy allowlist before forwarded IP/protocol headers are honored.
- HSTS outside Development and HTTPS redirection.
- Content Security Policy, frame denial, MIME sniffing prevention, strict referrer and permissions policies.
- No-store responses for authentication and SPA shell data.
- Long-lived immutable caching only for hashed build assets.
- Server-side token encryption at rest in the session cache.
- Bounded HTTP client timeout and explicit JSON protocol validation.
- Centralized, browser-safe error mapping with no exception details exposed.

## Error contract

BFF-owned errors use:

```json
{
  "code": "MACHINE_READABLE_CODE",
  "message": "User-facing message",
  "fieldErrors": {
    "fieldName": "Optional validation message"
  }
}
```

Java API errors are normalized into the same shape where possible. Malformed successful backend responses produce `502 BACKEND_PROTOCOL_ERROR`. Network failures and timeouts are distinguished in the UI.

## Adding a resource

For standard CRUD behavior:

1. Add Java and browser contracts under `Contracts`.
2. Add a typed backend client, normally through the generic CRUD base.
3. Add a controller with an explicit route and role requirement.
4. Register the client in `NexoraApiServiceCollectionExtensions`.
5. Add a typed `ResourceConfig` and route entry in the React application.
6. Add the navigation item with the same role group.
7. Update `docs/FEATURE_MATRIX.md` and add contract/integration tests.

Use a dedicated page and client method for workflows such as payment capture or enrollment cancellation; those operations are commands, not ordinary field updates.

## Production topology requirements

The repository defaults are suitable for a single local BFF process. A production deployment must provide shared session storage, persisted/shared Data Protection keys, TLS to the Java API, a restricted host allowlist, edge-level rate limiting for multiple replicas, centralized telemetry, and managed secrets. These open items are tracked in `FEATURE_MATRIX.md`.
