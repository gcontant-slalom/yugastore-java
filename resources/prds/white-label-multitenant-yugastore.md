# Auth-First White-Label Yugastore

## Problem Statement

The current Yugastore sample cannot support real user access because authentication is stubbed and inconsistent across the stack. The login service exists but is incomplete, the React UI has no register, login, or logout flows, the API gateway permits all traffic, and user-facing shopping flows still fall back to a hard-coded demo user. Before tenant, company, or broader white-label work can proceed, the product needs one minimum complete authentication capability that lets a user register with email and password, log in, and log out reliably.

## Goals

- Deliver the minimum complete authentication capability for email-and-password account creation, login, and logout.
- Make authentication the first implementation priority and explicit prerequisite for downstream platform work.
- Establish one consistent authenticated-state contract across the React UI, API gateway, login service, and persistence layer.
- Remove hard-coded demo-user reliance from the auth-critical request path.
- Keep planning context, sequencing, and constraints in repo-local PRD and OpenSpec artifacts.

## Non-Goals

- Add company or organization management.
- Implement customer onboarding or tenant onboarding flows.
- Add profile enrichment beyond what is required to store and identify an account.
- Add advanced RBAC or permissions beyond basic authenticated access.
- Implement forgot/reset password, email verification, MFA, magic links, social login, or SSO.
- Deliver merchant analytics, full white-label theming, or broader tenant isolation in this first slice.

## Background / Context

This PRD is based on:

- The source transcript under `resources/Eh AI Trio.docx`
- The prior PRD under `resources/prds/white-label-multitenant-yugastore.md`
- The current brownfield codebase across `react-ui`, `api-gateway-microservice`, `login-microservice`, `cart-microservice`, and `checkout-microservice`

The transcript still describes a broader white-label, multi-tenant target state, but it also explicitly calls out login as the first thing to fix and says the team needs to be able to log in as an internal user before expanding to external users and company concepts.

The current codebase confirms that auth is the immediate blocker:

- `login-microservice` contains Spring Security, password hashing, and JSP-based registration/login pages, but it is incomplete and currently modeled around `username` instead of email.
- `UserController` saves a user and redirects to login, and its auto-login path is commented out.
- `react-ui/frontend/src` has no login, registration, or logout flow at all.
- `api-gateway-microservice` currently permits every request.
- User-facing cart and checkout request paths still hard-code the demo user `u1001`.

Because of this, all downstream work for merchant administration, tenant context, company modeling, and external-user access is blocked on getting a real authentication foundation in place first.

## Architecture / Diagram References

- [resources/prds/diagrams/white-label-multitenant-yugastore-system-context.mmd](resources/prds/diagrams/white-label-multitenant-yugastore-system-context.mmd)

## Personas

- End user who needs to create an account, sign in, and sign out successfully.
- Frontend user of the Yugastore UI who needs a clear authenticated or unauthenticated state.
- Delivery team that needs an auth-first execution contract before planning tenant or merchant work.

## User Stories

- As a new user
  I want to create an account with my email and password
  So that I can access authenticated parts of the application.

- As a returning user
  I want to log in with valid email and password credentials
  So that the application recognizes me consistently across requests.

- As an authenticated user
  I want to log out cleanly
  So that my browser and server-side auth state are cleared.

- As a delivery team member
  I want authentication to be the first bounded implementation slice
  So that downstream tenant and merchant work builds on a real user identity instead of a stub.

## Functional Requirements

- The system must allow a user to create an account using email and password.
- Registration must validate email format before account creation succeeds.
- Registration must enforce a minimum password standard and reject invalid passwords clearly.
- Registration must reject duplicate accounts for the same email with a clean, user-visible error.
- The system must authenticate a user with valid email and password credentials.
- Login must reject invalid credentials with a clear error outcome.
- The system must establish authenticated state that persists appropriately for subsequent requests in the chosen browser or API session mechanism.
- The system must provide a logout flow that clears the active session or token state successfully.
- The React UI must expose register, login, and logout entry points rather than relying on server-rendered JSP-only flows.
- The API gateway must stop treating all requests as anonymous by default for auth-protected flows and must stop relying on a hard-coded demo user for the auth-critical path.
- The minimum authenticated identity contract must be usable by downstream services as the basis for future protected access and later tenant-aware behavior.
- Authentication must be implemented and verified before company/org modeling, tenant onboarding, customer onboarding, or broader merchant features are planned for implementation.

## Non-Functional Requirements

- Passwords must be stored securely using a one-way password hash rather than plaintext.
- Validation and error handling must be basic but clear across registration, login, and logout.
- The auth approach must stay lightweight and avoid unnecessary identity-platform complexity in the first slice.
- The browser-facing auth state mechanism must be documented clearly in PRD and OpenSpec artifacts.
- The solution must stay aligned with the brownfield Spring Boot, Maven, React 16, and YugabyteDB structure already in the repository.

## Constraints

- `login-microservice` exists but is incomplete and currently uses a legacy `username`-oriented model.
- `login-microservice` and `cart-microservice` use YSQL/PostgreSQL-compatible persistence.
- `products-microservice` and `checkout-microservice` use YCQL.
- The React UI currently has no auth UI or auth state management.
- The API gateway currently permits all requests and therefore does not yet behave as an authenticated boundary.
- Some user-facing request paths still rely on hard-coded demo-user values.

## Assumptions

- `resources/Eh AI Trio.docx` remains the canonical source transcript for this initiative.
- The existing `login-microservice` should become the source of truth for user credentials and password hashes rather than introducing a brand-new auth service.
- Email is the external credential identifier for the first auth slice even if legacy `username` fields need migration or compatibility handling internally.
- The first auth slice will use one consistent app-facing session or token mechanism across browser, gateway, and login service instead of mixing JSP-only login state with separate frontend behavior.
- Broader tenant, company, and merchant-administration work will remain deferred until the auth foundation is complete.

## Open Questions

- Should the app-facing authenticated state be implemented as a gateway-mediated secure cookie session, a signed token cookie, or another equally lightweight mechanism?
- How should legacy `username` records be migrated or mapped once email becomes the public login identifier?
- Which currently anonymous endpoints should remain public after the first auth slice versus becoming protected immediately?

## Risks

- Mixing the legacy JSP login flow with a new React-facing auth flow could create two competing contracts if not unified.
- Replacing hard-coded demo-user behavior may expose additional downstream assumptions in cart and checkout flows.
- If the auth state mechanism is unclear or inconsistent, logout and authenticated persistence behavior will be fragile.
- If tenant or merchant features are allowed to start before auth is complete, the repo will reintroduce conflicting priorities and incomplete user identity assumptions.

## Dependencies

- `login-microservice`, `api-gateway-microservice`, and `react-ui` must agree on register, login, logout, and current-user behavior.
- YSQL-backed user persistence and any related schema under `resources/schema.sql` must support unique email-based accounts and secure password storage.
- The brownfield user-facing request path needs a consistent authenticated identity contract before the hard-coded demo user can be removed from protected flows.
- Repo-local PRD and OpenSpec artifacts must stay aligned so the team does not plan downstream tenant work ahead of auth completion.

## Candidate OpenSpec Changes

- `merchant-auth-foundation`: deliver email/password registration, login, logout, authenticated-state handling, and current-user propagation as the first bounded slice.
- `merchant-tenant-foundation`: add company/store and tenant-context foundations only after auth is complete.
- `merchant-catalog-isolation`: scope product-management behavior by authenticated merchant or store context after auth and tenant foundations exist.
- `merchant-ops-reporting`: add merchant-specific operational views after auth and tenant-aware data ownership are in place.
- `ai-delivery-collaboration-foundation`: continue refining repo-local workflow and coordination artifacts in parallel when needed.

## OpenSpec Change Dependencies

- `merchant-auth-foundation` must complete before `merchant-tenant-foundation`, `merchant-catalog-isolation`, and `merchant-ops-reporting`.
- `merchant-tenant-foundation` remains blocked until a real authenticated user or session contract exists.
- `merchant-catalog-isolation` and `merchant-ops-reporting` remain blocked until both auth and tenant foundations are complete.
- `ai-delivery-collaboration-foundation` can proceed in parallel because it supports delivery workflow rather than product authentication behavior.

## Recommended Sequencing

- First, implement and verify `merchant-auth-foundation`.
- Second, remove any remaining protected-flow reliance on the hard-coded demo user as part of the auth rollout or immediate follow-through.
- Third, resume `merchant-tenant-foundation` using the real authenticated user identity as the upstream contract.
- Fourth, layer on tenant-aware catalog and operational capabilities.
- Fifth, add broader white-label and merchant-management behavior only after auth and tenant contracts are stable.

## Success Metrics

- A user can register with email and password successfully.
- Duplicate email registration is rejected cleanly.
- A user can log in with valid credentials and receives a stable authenticated state.
- Invalid login attempts fail with clear error handling.
- A user can log out and lose authenticated access until logging in again.
- Auth-critical request paths no longer depend on the hard-coded demo user.
- The repo's PRD and OpenSpec artifacts clearly show authentication as Priority 1 and downstream tenant work as blocked until auth is complete.