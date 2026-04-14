# Merchant Auth Foundation

## Problem Statement

The current Yugastore sample still relies on mock or incomplete authentication, which blocks the broader merchant and multi-tenant work described in the requirements transcript. Before company onboarding, tenant routing, or merchant-owned catalogs can work reliably, the application needs a minimum complete authentication foundation for real users.

## Goals

- Deliver registration, login, and logout for real users.
- Establish one browser-facing authenticated-state contract across the React UI, API gateway, and login service.
- Remove hard-coded demo-user assumptions from auth-critical request paths.
- Make authentication the explicit prerequisite for tenant and merchant planning.

## Non-Goals

- Company or merchant onboarding.
- Tenant assignment or tenant-aware routing.
- Merchant employee roles or advanced authorization.
- Password reset, MFA, SSO, or social login.
- Merchant catalog administration, dashboards, reporting, or fraud workflows.

## Background / Context

This PRD is derived from [resources/requirements.pdf](../requirements.pdf), which describes the current effort as a brownfield modernization moving toward white-label, multi-tenant Yugastore behavior. The transcript explicitly separates the shopper flow from the company flow and notes that login is a prerequisite that needs to be enabled before the larger merchant flow can be demonstrated.

The current repository state matches that concern:

- the React UI does not expose a complete auth experience
- the login service is incomplete
- the API gateway does not yet provide a consistent auth boundary
- some user-facing paths still depend on hard-coded demo-user behavior

## Architecture / Diagram References

- [resources/prds/diagrams/merchant-platform-context.mmd](diagrams/merchant-platform-context.mmd)

## Personas

- Shopper who needs to create an account and sign in.
- Returning user who needs a reliable authenticated session.
- Delivery team that needs auth to be complete before merchant and tenant work proceeds.

## User Stories

- As a new shopper
  I want to register with email and password
  So that I can access authenticated shopping flows.

- As a returning shopper
  I want to log in and remain authenticated across requests
  So that the application recognizes me consistently.

- As a delivery team member
  I want auth to be a distinct first slice
  So that downstream merchant work does not build on mock identity behavior.

## Functional Requirements

- The system must allow a user to register with email and password.
- Registration must reject duplicate accounts and invalid input clearly.
- The system must allow a user to log in with valid credentials.
- Login must reject invalid credentials clearly.
- The system must establish a browser-facing authenticated state that can be reused on subsequent requests.
- The system must provide logout that clears the active authenticated state.
- The React UI must expose registration, login, and logout entry points.
- Auth-protected request paths must resolve the authenticated user instead of falling back to a hard-coded demo user.

## Non-Functional Requirements

- Passwords must be stored as secure one-way hashes.
- Auth behavior must remain consistent across the React UI, API gateway, and login service.
- The first auth slice must stay lightweight and avoid unnecessary identity-platform complexity.
- The auth contract must be documented well enough to unblock tenant and merchant planning.

## Constraints

- The codebase is a brownfield Spring Boot and React application.
- The login service exists but is incomplete.
- The React UI currently lacks a complete auth flow.
- The gateway does not yet enforce a consistent authenticated boundary.

## Assumptions

- Email is the public login identifier for the first slice.
- The existing login microservice remains the source of truth for user credentials.
- One consistent browser-facing session approach will be used rather than parallel JSP-only and React-only auth contracts.

## Open Questions

- Should the first slice use a gateway-managed session cookie, a signed auth cookie, or another lightweight session mechanism?
- Which currently public endpoints should remain anonymous after auth is introduced?

## Risks

- Mixing legacy server-rendered auth behavior with a new React-facing flow could create competing contracts.
- Removing the hard-coded demo-user path may expose downstream assumptions in cart or checkout behavior.

## Dependencies

- `login-microservice`, `api-gateway-microservice`, and `react-ui` must agree on the auth contract.
- Downstream tenant and merchant work depends on this auth slice being complete.

## Candidate OpenSpec Changes

- `merchant-auth-foundation`: deliver registration, login, logout, and current-user handling as the first bounded slice.

## OpenSpec Change Dependencies

- none

## Recommended Sequencing

- First, complete `merchant-auth-foundation`.
- Second, begin tenant and merchant onboarding work on top of the real auth contract.

## Success Metrics

- A user can register successfully with email and password.
- Duplicate registration is rejected clearly.
- A user can log in and remain authenticated across subsequent requests.
- A user can log out and lose authenticated access.
- Auth-critical paths no longer depend on the hard-coded demo user.