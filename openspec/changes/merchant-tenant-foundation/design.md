## Context

The broader roadmap still targets a white-label platform for multiple merchant organizations. However, the product is now explicitly auth-first: the application must first support email/password registration, login, and logout. The requirements transcript then narrows the next slice to merchant company onboarding, tenant assignment, and a local-demo-friendly tenant route.

This design keeps the tenant change focused on post-auth company, store, onboarding, and ownership foundations that catalog isolation and reporting work can build on.

## Goals / Non-Goals

**Goals:**
- Establish a company or store ownership model on top of authenticated user identity.
- Add a bounded merchant signup path that can be shared as a link.
- Resolve tenant context from a browser-visible slug path for local demos.
- Carry tenant context through the targeted request path instead of relying on hard-coded demo-user values.
- Persist tenant ownership on targeted merchant-owned records in the first slice.
- Keep the change small enough to support dependency-ordered implementation and later issue generation.

**Non-Goals:**
- Rebuild the authentication system.
- Add invitation-token security, custom-domain routing, or full merchant role management.
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

### 3. Use a simple shared merchant signup route in the first slice

Recommended decision: start with a merchant onboarding route that can be shared directly by internal staff instead of designing invitation tokens or a more complex provisioning workflow.

- Why: the transcript asks for a link a salesperson can send and does not require token lifecycle management in the first milestone.
- Alternative considered: one-time invitation or approval-token flows.
- Rejected for now because they add scope without changing the first demo outcome.

### 4. Resolve tenant context from a path-based browser route for the first slice

Recommended decision: use path-based tenant selection for the first implementation and local demo, then allow domain-based routing later if the product needs it.

- Why: the transcript explicitly says path-based routing is acceptable and custom domains are unnecessary for the demo.
- Alternative considered: subdomain or custom-domain tenant routing immediately.
- Rejected for now because it adds setup complexity that is not required to prove the workflow.

### 4a. Make the slug route contract canonical and explicit

Recommended decision: reserve the demo storefront root at `/`, tenant storefront routes at `/{tenantSlug}/`, and merchant signup routes at `/{tenantSlug}/signup` as the only supported local first-slice route shapes.

- Why: the current planning says path-based routing is supported, but the route shapes need to be fixed so frontend routing, gateway handling, verification, and merchant-facing URLs do not drift. Keeping `/` as the demo store also prevents tenant failures from silently collapsing into a generic landing experience.
- Alternative considered: allow multiple equivalent route patterns such as `/store/{tenantSlug}` or a root-level signup path.
- Rejected because multiple valid patterns would make demos and downstream tenant resolution ambiguous.

### 4b. Validate slug uniqueness and format during onboarding

Recommended decision: require tenant slugs to be unique, lowercase, browser-safe identifiers created during merchant signup and rejected explicitly when invalid or already taken.

- Why: merchants need a stable storefront path and the platform needs deterministic tenant resolution.
- Alternative considered: generate opaque tenant ids internally and let friendly slugs be optional.
- Rejected for now because the first slice needs a human-readable URL that can be shared directly.

### 4c. Keep onboarding success confirmation transient to the completed submit flow

Recommended decision: show the merchant-tenant-created confirmation only as transient post-submit state and clear it when the user leaves the onboarding page, reloads it, or later returns without a fresh successful submission.

- Why: the confirmation communicates the outcome of a single create-tenant action. If it survives navigation or reload through sticky client state, the onboarding screen misrepresents the current page state and can lead merchants to think a new creation just completed.
- Alternative considered: persist the last-success banner in local storage, shared application state, or query parameters so the user always sees their most recent result.
- Rejected because the screen already has a tenant list for historical context, and a stale success banner is a misleading action confirmation rather than durable account data.

### 5. Resolve tenant context at the gateway boundary using authenticated identity and route context as input

Recommended decision: treat tenant context as a request-scoped contract introduced at or before the API gateway and derived from the authenticated user or merchant association plus the supported tenant route.

- Why: the gateway already coordinates multiple downstream services and is the narrowest place to translate authenticated identity into downstream tenant context.
- Alternative considered: let each downstream service derive tenant context independently.
- Rejected because it would duplicate logic and make future auth integration harder.

### 6. Seed existing demo data under a default merchant context during migration

Recommended decision: preserve current demo usability by mapping existing sample data to one default merchant or store context while the tenant model is introduced.

- Why: this supports incremental rollout without requiring an immediate full onboarding experience.
- Alternative considered: require all existing sample data to be recreated manually.
- Rejected because it creates unnecessary friction for verification and local startup.

### 7. Define one explicit first-slice merchant-context contract

Recommended decision: keep the first-slice merchant context intentionally small and align it with the fields already flowing through the login service, gateway, and tenant storefront routes.

- Merchant company or store context is the shared tenant object persisted in `login-microservice` as `MerchantTenant`.
- The first-slice merchant-context response shape is:
	- `tenantId`: stable tenant identifier for the created or resolved merchant tenant
	- `tenantKey`: browser-visible slug used in tenant storefront and signup routes
	- `companyName`: merchant-facing display name for the tenant context
	- `merchantAdminUserId`: authenticated user currently acting as the merchant-admin anchor for the response
- User-to-tenant association remains separate from shopper identity through `MerchantMembership`, which links one or more authenticated users to the same merchant tenant.
- Request context for the bounded storefront flow is derived in this order:
	- `/` means the shared demo storefront and does not trigger tenant-slug lookup
	- `/{tenantSlug}/...` means resolve tenant context by slug at the gateway boundary
	- authenticated shopper or merchant identity remains a separate concern from the resolved merchant tenant
- For the first bounded downstream flow, the gateway forwards merchant tenant context as `X-Tenant-Key` and `X-Merchant-Company-Name`; authenticated user identity continues to use `X-Authenticated-UserId` where that contract already exists.
- The shared root storefront is the default merchant-context mapping for the current demo dataset. Concretely, the existing `cronos.products`, `cronos.product_rankings`, and `cronos.product_inventory` sample data are treated as one implicit demo merchant context served only at `/` until later persistence work adds explicit ownership fields.

- Why: this matches the current implementation surface and gives later ownership work a stable contract to build on.
- Alternative considered: define a broader merchant/store/request object now with separate store ids, theme ids, and catalog ids.
- Rejected for now because those fields are not yet implemented and would create speculative contract surface.

## Risks / Trade-offs

- [Auth contract changes late] -> Keep tenant-context inputs dependent on the finalized auth-first user identity contract.
- [Merchant and shopper identity boundaries blur] -> Model merchant company or store ownership separately from shopper identity from the start.
- [Hidden hard-coded user assumptions in other modules] -> Constrain the initial change to a targeted request path and add focused tests around context propagation.
- [Mixed persistence models increase migration complexity] -> Keep ownership fields simple and service-specific, and avoid schema-wide rewrites in the first slice.
- [Future schema-per-tenant design may still be desired] -> Treat row-level ownership as a foundational step, not a permanent exclusion of later isolation changes.

## Migration Plan

- Add a merchant onboarding route and company or store creation contract for the first slice.
- Keep the first-slice merchant-context response limited to `tenantId`, `tenantKey`, `companyName`, and `merchantAdminUserId`, with user-to-tenant links represented separately through membership.
- Fix the canonical route contract to `/`, `/{tenantSlug}/`, and `/{tenantSlug}/signup`.
- Ensure the onboarding page loads with a clean form state unless the user has just completed a successful tenant-creation submit in the current flow.
- Add tenant ownership fields to targeted merchant-owned data structures and seed assets.
- Treat the existing `cronos` sample catalog and storefront behavior at `/` as the implicit default merchant context so existing sample behavior remains testable while explicit ownership rollout is still pending.
- Update frontend and gateway routing so a supported path resolves tenant context for local demos.
- Update the gateway and downstream contract to pass tenant context on the targeted request path after auth identity is available.
- Add focused verification for missing-context rejection and ownership persistence.
- If rollback is needed, retain the default merchant mapping and revert only the ownership-aware contract changes.

## Open Questions

- What minimum merchant-company fields are required beyond company or store name?
- Should merchant onboarding create its first merchant-admin account in the same flow or as a follow-up step?
- What exact slug validation rules should apply beyond lowercase browser-safe formatting and uniqueness?
- Which first-slice endpoints beyond product and checkout must be tenant-aware to be considered complete?
- Does `cart-microservice` need to participate in the initial foundation, or can it be deferred to a later change?