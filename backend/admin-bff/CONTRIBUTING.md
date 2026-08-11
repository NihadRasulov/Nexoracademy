# Contributing to Nexora Academy Admin Panel

This repository contains a React admin SPA and an ASP.NET Core Backend-for-Frontend (BFF). Keep changes inside the correct boundary: the browser calls the BFF, and only the BFF calls the Nexora Java API.

## Prerequisites

- .NET 10 SDK
- Node.js 20 or newer
- npm
- Access to a compatible Nexora Java API for integration testing

## Local setup

```bash
cd NexoraAdminPanelUI
npm ci
npm run dev
```

In a second terminal:

```bash
cd NexoraAdminPanel/NexoraAdminPanel/src/NexoraAcademy.AdminBff
dotnet restore
dotnet run
```

The development URL is derived from `AdminSettings:SecretPath`. Never hard-code this prefix in React components, controllers, or tests.

## Required checks

Run these before handing a change to another developer:

```bash
cd NexoraAdminPanelUI
npm run check
```

```bash
cd NexoraAdminPanel/NexoraAdminPanel
dotnet build NexoraAdminPanel.sln --no-restore
dotnet format NexoraAdminPanel.sln --verify-no-changes --no-restore
```

A release-affecting change must also pass:

```bash
cd NexoraAdminPanel/NexoraAdminPanel/src/NexoraAcademy.AdminBff
dotnet publish -c Release
```

## Design rules

- Never return backend access or refresh tokens to the browser.
- Put backend communication in a typed client under `Clients/`; controllers should coordinate requests, not implement HTTP protocol details.
- Put cross-cutting HTTP behavior in middleware or service-registration extensions.
- Enforce authorization in the BFF even when the UI hides a route or action.
- Keep contracts bounded with validation attributes. Do not forward arbitrary, unbounded input.
- Return the standard JSON error shape (`code`, `message`, optional `fieldErrors`) from BFF-owned failures.
- Use TanStack Query for server state and the shared API client for HTTP calls.
- Prefer the generic resource infrastructure for ordinary CRUD screens; create a dedicated page only for domain-specific workflows.
- Lazy-load new top-level pages to protect initial load performance.
- Icon-only controls must have an accessible name.

## Comments and naming

Code comments must be in English and should explain intent, constraints, or a non-obvious trade-off. Do not comment syntax or repeat what a method name already says. User-facing interface text remains in Azerbaijani.

Use the existing domain vocabulary consistently. Avoid abbreviations unless they are already part of the domain, such as BFF, API, CMS, CRM, or OTP.

## Configuration and secrets

- Do not commit production secrets, tokens, passwords, private keys, or real infrastructure addresses.
- Prefer environment variables or the deployment platform's secret manager.
- Treat `AdminSettings:SecretPath` as defense in depth, not as authentication.
- Keep `ReverseProxy:KnownProxies` restricted to infrastructure controlled by the deployment owner.
- Use HTTPS for the BFF-to-Java API connection outside local development.

## Pull request checklist

- The relevant build, lint, formatting, and tests pass.
- Authentication and role boundaries are unchanged or explicitly reviewed.
- New configuration is validated at startup and documented.
- Failure, empty, loading, and unauthorized UI states are covered.
- Logs contain useful identifiers but no credentials, tokens, OTP values, or sensitive personal data.
- `docs/FEATURE_MATRIX.md` is updated when a capability is added or retired.
