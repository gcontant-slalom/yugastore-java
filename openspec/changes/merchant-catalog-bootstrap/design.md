## Context

The requirements transcript describes merchant inventory upload and operations as longer-term needs, but it explicitly allows the current milestone to seed data and pretend the full admin interfaces exist. This change keeps the scope intentionally small: prove that tenants can own distinct starter catalogs and that shoppers only see and buy products for the active tenant.

The repository already stores product-related sample data under `resources/` and routes shopper behavior through the API gateway into YCQL-backed product and checkout services. That makes a seed-first tenant catalog slice practical after `merchant-tenant-foundation` establishes tenant context.

## Goals / Non-Goals

**Goals:**
- Associate starter catalog data to tenant ownership.
- Filter storefront catalog results by the active tenant.
- Keep cart and checkout behavior scoped to the active tenant for the first flow.
- Reuse checked-in seed assets instead of introducing a full merchant admin UI.

**Non-Goals:**
- Build merchant-facing catalog CRUD screens.
- Build payment-gateway setup, low-stock alerts, reporting, or fraud tooling.
- Introduce cross-tenant carts or blended multi-store checkout behavior.

## Decisions

### 1. Use checked-in seed assets for the first catalog bootstrap slice

Recommended decision: extend the existing sample-data workflow under `resources/` so tenant-owned starter catalogs can be created without building a merchant upload interface.

- Why: the transcript explicitly allows seeding instead of full admin screens for the current milestone.
- Alternative considered: build CSV upload or merchant catalog CRUD UI immediately.
- Rejected for now because it expands the slice without changing the core demo outcome.

### 2. Persist tenant ownership on product and targeted shopping records

Recommended decision: store tenant ownership on product records and the targeted cart or checkout records created by the first flow.

- Why: the active tenant must remain observable and enforceable from browsing through purchase.
- Alternative considered: filter only at read time and leave downstream records unowned.
- Rejected because it would make ownership drift and verification harder.

### 3. Filter catalog reads and shopping writes by propagated tenant context

Recommended decision: use the tenant context introduced by `merchant-tenant-foundation` as the single filter input for product reads and shopping writes in the first slice.

- Why: it keeps the ownership model consistent from route selection to downstream persistence.
- Alternative considered: let each service infer tenant from ad hoc request details.
- Rejected because it duplicates logic and weakens isolation guarantees.

### 4. Keep the first shopping flow tenant-scoped end to end

Recommended decision: require the first cart and checkout flow to stay within one active tenant context.

- Why: the transcript asks for a demonstrable purchase flow from one tenant, not a cross-tenant shopping network.
- Alternative considered: allow mixed-tenant carts in the first slice.
- Rejected because it would expand both data model and UX scope prematurely.

## Risks / Trade-offs

- [Tenant context does not propagate consistently] -> Keep the first slice narrow and verify context in product, cart, and checkout boundaries.
- [Seed workflow becomes a hidden permanent dependency] -> Document it as a bootstrap mechanism and keep full merchant-admin tooling explicitly out of scope.
- [Cart behavior may already assume global products] -> Treat cart participation as targeted and verify only the scoped first flow.

## Migration Plan

- Extend sample-data assets to represent at least two tenant-owned starter catalogs.
- Add tenant ownership fields to the targeted product and shopping records touched by the first flow.
- Update gateway and service contracts so active tenant context filters reads and scopes writes.
- Verify browsing and purchase behavior for at least two tenants.
- If rollback is needed, preserve existing sample data and revert only the new tenant ownership and filtering behavior.

## Open Questions

- Which seed asset format should become the canonical source for tenant-owned starter catalogs?
- Does `cart-microservice` need ownership persisted in the first slice, or can checkout alone be the first owned shopping record?
- Which checkout records must store tenant ownership to make the first demo and future reporting reliable?