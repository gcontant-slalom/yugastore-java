## Context

The broader roadmap still targets a white-label platform for multiple merchant organizations. However, the product is now explicitly auth-first: the application must first support email/password registration, login, and logout. This change begins only after `merchant-auth-foundation` provides a real authenticated user and session contract that downstream services can trust.

This design keeps the tenant change focused on post-auth company, store, and ownership foundations that catalog isolation and reporting work can build on.

## Goals / Non-Goals

**Goals:**
- Establish a company or store ownership model on top of authenticated user identity.
- Carry tenant context through the targeted request path instead of relying on hard-coded demo-user values.
- Persist tenant ownership on targeted merchant-owned records in the first slice.
- Keep the change small enough to support dependency-ordered implementation and later issue generation.

**Non-Goals:**
- Rebuild the authentication system.
- Rebuild every service to be fully tenant-aware in this first change.
- Deliver merchant analytics or theming.

## Decisions

### 1. Sequence tenant foundation after auth completion

Recommended decision: treat authenticated user identity as an upstream dependency and start tenant work only after auth is working end to end.

- Why: the user directive and transcript both prioritize fixing login first, and downstream tenant behavior is safer once a real user identity exists.
- Alternative considered: continue with a tenant-first request-context abstraction.
- Rejected because it would preserve stubbed identity assumptions and create contradictory planning.

### 2. Use explicit row-level ownership metadata in the first slice

Recommended decision: add explicit company or store ownership fields to targeted merchant-owned records for the first slice rather than adopting schema-per-tenant immediately.

- Why: the transcript mentions schemas as one possible approach, but row-level ownership is a smaller and safer foundation for a mixed YCQL and YSQL repository.
- Alternative considered: schema-per-tenant isolation.
- Rejected for now because it creates more migration and operational complexity before the product workflow is proven.

### 3. Resolve tenant context at the gateway boundary using authenticated identity as input

Recommended decision: treat tenant context as a request-scoped contract introduced at or before the API gateway and derived from the authenticated user or merchant association available after auth completion.

- Why: the gateway already coordinates multiple downstream services and is the narrowest place to translate authenticated identity into downstream tenant context.
- Alternative considered: let each downstream service derive tenant context independently.
- Rejected because it would duplicate logic and make future auth integration harder.

### 4. Seed existing demo data under a default merchant context during migration

Recommended decision: preserve current demo usability by mapping existing sample data to one default merchant or store context while the tenant model is introduced.

- Why: this supports incremental rollout without requiring an immediate full onboarding experience.
- Alternative considered: require all existing sample data to be recreated manually.
- Rejected because it creates unnecessary friction for verification and local startup.

## Risks / Trade-offs

- [Auth contract changes late] -> Keep tenant-context inputs dependent on the finalized auth-first user identity contract.
- [Hidden hard-coded user assumptions in other modules] -> Constrain the initial change to a targeted request path and add focused tests around context propagation.
- [Mixed persistence models increase migration complexity] -> Keep ownership fields simple and service-specific, and avoid schema-wide rewrites in the first slice.
- [Future schema-per-tenant design may still be desired] -> Treat row-level ownership as a foundational step, not a permanent exclusion of later isolation changes.

## Migration Plan

- Add tenant ownership fields to targeted merchant-owned data structures and seed assets.
- Map current demo data to a default merchant or store context so existing sample behavior remains testable.
- Update the gateway and downstream contract to pass tenant context on the targeted request path after auth identity is available.
- Add focused verification for missing-context rejection and ownership persistence.
- If rollback is needed, retain the default merchant mapping and revert only the ownership-aware contract changes.

## Open Questions

- How should authenticated user identity map to merchant company or store membership in the first tenant slice?
- Which first-slice endpoints beyond product and checkout must be tenant-aware to be considered complete?
- Does `cart-microservice` need to participate in the initial foundation, or can it be deferred to a later change?