## Context

The brownfield application already contains fragments of authentication logic, but they do not produce a working end-to-end user flow. `login-microservice` uses Spring Security and BCrypt, yet it is still oriented around `username`, exposes JSP views, and is incomplete. `react-ui` has no register, login, or logout flow. `api-gateway-microservice` permits every request. User-facing flows still rely on the hard-coded demo user `u1001`.

This change establishes the minimum complete authentication foundation before any tenant, company, or broader white-label work proceeds.

## Goals / Non-Goals

**Goals:**
- Support user registration with email and password.
- Support login with valid email and password and reject invalid credentials clearly.
- Support logout that clears the active authenticated state.
- Use one consistent app-facing authenticated-state mechanism across browser, gateway, and auth service.
- Keep the scope small enough for one bounded OpenSpec change.

**Non-Goals:**
- Add tenant onboarding, company management, or merchant modeling.
- Add password reset, email verification, MFA, magic links, SSO, or social login.
- Add advanced RBAC beyond the minimum needed for authenticated access.
- Rebuild the entire brownfield app around a new identity platform.

## Decisions

### 1. Keep `login-microservice` as the source of truth for user credentials

Recommended decision: extend the existing YSQL-backed login service instead of introducing a new auth service.

- Why: the repository already has a login module, password hashing, user persistence, and Spring Security foundations.
- Alternative considered: create a brand-new auth microservice.
- Rejected because it adds unnecessary service sprawl for a minimum auth slice.

### 2. Make email the public login identifier

Recommended decision: define email as the external credential identifier even if legacy `username` fields need compatibility handling or migration internally.

- Why: the requested capability is explicitly email plus password registration and login.
- Alternative considered: preserve `username` as the primary login contract.
- Rejected because it conflicts with the requested product behavior.

### 3. Use one browser-facing authenticated-state mechanism

Recommended decision: use a single browser-facing auth session or token mechanism across the React UI and API gateway instead of relying on JSP-only login state or mixed client-side patterns.

- Why: the current split between JSP auth and React UI is one of the root causes of the stubbed experience.
- Alternative considered: keep legacy JSP form login for auth and add separate frontend-only state later.
- Rejected because it creates two competing auth contracts.

### 4. Keep the API gateway as the external auth boundary

Recommended decision: the React app should continue talking to the API gateway, and the gateway should expose or proxy the register, login, logout, and current-user contract consistently.

- Why: the UI already treats the gateway as its backend entry point.
- Alternative considered: have the React app call `login-microservice` directly while using the gateway for everything else.
- Rejected because it would split browser-facing backend behavior across multiple services.

### 5. Block downstream tenant and merchant work until auth is complete

Recommended decision: finish this change before resuming tenant context, company modeling, catalog isolation, or merchant operations work.

- Why: downstream features need a real authenticated user identity instead of a stub.
- Alternative considered: plan auth and tenant work in parallel as equal priorities.
- Rejected because the repo already shows that missing auth causes contradictory and incomplete behavior.

### 6. Use a gateway-owned session contract for the browser

Recommended decision: define the browser-facing auth contract at the API gateway using a session-backed current user, while the login service remains the credential authority for registration and login validation.

- External contract:
	- `POST /api/v1/auth/register` creates an account and returns `{ userId, email }` with `201 Created`
	- `POST /api/v1/auth/login` validates credentials, stores the authenticated user in the gateway session, and returns `{ userId, email }`
	- `POST /api/v1/auth/logout` clears the gateway session and returns `204 No Content`
	- `GET /api/v1/auth/current-user` returns `{ userId, email }` for the current browser session or `401 Unauthorized`
- Why: this keeps the React app talking to one backend boundary and avoids leaking login-service session behavior directly into the browser contract.
- Alternative considered: preserve form-login state in the login service and have the UI interact with it directly.
- Rejected because it splits the browser contract across multiple services and does not fit the existing gateway entrypoint.

## Risks / Trade-offs

- [Legacy username model complicates migration] -> Keep email as the public contract and document any temporary compatibility mapping clearly.
- [Gateway and auth service can drift] -> Centralize the browser-facing auth contract at the gateway boundary.
- [Logout can be underspecified] -> Define explicit logout behavior that clears the active session or token and verify it with tests.
- [Hard-coded demo-user assumptions may extend beyond the first touched flow] -> Remove them from the auth-critical path first and record any residual follow-up explicitly.

## Migration Plan

- Update the user credential model and persistence contract to support unique email-based accounts.
- Implement registration, login, logout, and current-user behavior in the brownfield auth path.
- Expose a consistent auth contract through the API gateway for the React app.
- Add React flows for registration, login, logout, and authenticated-state handling.
- Remove auth-critical hard-coded demo-user assumptions from the user-facing request path.
- If rollback is needed, preserve user records and revert only the new gateway and UI auth integration paths.

## Open Questions

- Should the final browser-facing mechanism be a secure cookie-backed session, a signed token cookie, or an equivalent lightweight pattern?
- Which currently public endpoints should remain public in the first auth slice versus becoming protected immediately?
- Does the first auth rollout need a current-user endpoint explicitly, or can authenticated identity be inferred from protected endpoint responses alone?