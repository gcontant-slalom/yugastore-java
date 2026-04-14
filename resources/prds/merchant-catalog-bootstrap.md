# Merchant Catalog Bootstrap

## Problem Statement

The requirements transcript expects each tenant to have its own products and a demonstrable purchase flow, but it also explicitly allows the team to defer full admin interfaces and seed the data instead. Yugastore therefore needs a bounded catalog bootstrap slice that associates catalog data to merchant tenants and demonstrates tenant-specific shopping behavior without requiring a complete merchant back office.

## Goals

- Associate product catalog data to merchant tenant ownership.
- Allow the first implementation to seed tenant-specific catalog data instead of building full admin upload interfaces.
- Demonstrate at least two tenants with distinct products.
- Keep shopping, cart, and checkout behavior scoped to the selected tenant.

## Non-Goals

- Full merchant catalog CRUD UI.
- CSV upload UI or bulk import screens.
- Payment gateway setup during merchant onboarding.
- Low-stock alerts, reporting, fraud detection, or warehouse dashboards.
- Cross-tenant carts or blended multi-store checkout behavior.

## Background / Context

This PRD is derived from [resources/requirements.pdf](../requirements.pdf). The transcript says merchants eventually need to upload their inventory and manage it, but for the current milestone the team can seed data instead of building all admin interfaces. It also says the demo should show at least two tenants with different products and allow the audience to buy something from one of them.

The repository already includes product and data-loading assets under `resources/`, which makes a seed-based bootstrap slice practical.

## Architecture / Diagram References

- [resources/prds/diagrams/merchant-platform-context.mmd](diagrams/merchant-platform-context.mmd)
- [resources/prds/diagrams/merchant-catalog-bootstrap-flow.mmd](diagrams/merchant-catalog-bootstrap-flow.mmd)

## Personas

- Merchant who needs a tenant-specific starter catalog.
- Shopper who needs to browse and buy products from a selected tenant.
- Delivery team that needs a credible demo without full admin tooling.

## User Stories

- As a merchant
  I want my store to have its own starter catalog
  So that shoppers see products that belong to my tenant.

- As a shopper
  I want to browse and buy products for the selected store only
  So that my shopping experience matches the active tenant.

- As a delivery team member
  I want to seed tenant-owned catalog data
  So that we can demonstrate the merchant concept before building full management screens.

## Functional Requirements

- The system must associate product catalog records with tenant ownership.
- The initial implementation may populate tenant-owned catalog data through seed assets instead of merchant-admin UI.
- The seed workflow must support at least two tenants with distinct product assortments.
- Storefront product browsing must show products for the active tenant only.
- Cart and checkout behavior for the first slice must remain scoped to the active tenant.
- The bootstrap workflow must support adding starter inventory for newly onboarded merchant tenants.

## Non-Functional Requirements

- Seed-based setup must be documented well enough for local demo use.
- The first slice must minimize UI surface area and favor checked-in sample data.
- Tenant ownership behavior must be observable in product browsing and purchase flows.

## Constraints

- `merchant-tenant-foundation` must establish tenant context first.
- Product and checkout services use YCQL-backed persistence patterns in this repository.
- Existing brownfield data-loading flows need to be extended rather than replaced wholesale.

## Assumptions

- The first slice uses checked-in seed assets under `resources/`.
- Full merchant upload and inventory-management interfaces remain deferred.
- Cart and checkout state are tenant-scoped in the first implementation.

## Open Questions

- Which seed asset format should become the source of truth for tenant-owned catalog bootstrap?
- Does the first slice need per-tenant inventory quantity updates, or only distinct product catalogs and a purchase path?
- Which checkout records need explicit tenant ownership persisted in the first implementation?

## Risks

- If product ownership is added without consistent tenant propagation, tenants may see each other’s catalog entries.
- If the bootstrap slice grows into a full admin surface, it will delay the broader demo goal.

## Dependencies

- `merchant-tenant-foundation` must provide tenant context.
- `products-microservice`, `checkout-microservice`, and sample data assets under `resources/` must align on tenant ownership.

## Candidate OpenSpec Changes

- `merchant-catalog-bootstrap`: add tenant-owned catalog seed data, tenant-filtered storefront behavior, and tenant-scoped shopping flows.

## OpenSpec Change Dependencies

- `merchant-catalog-bootstrap` depends on `merchant-tenant-foundation`.

## Recommended Sequencing

- First, complete `merchant-tenant-foundation`.
- Second, add tenant-owned seed data and storefront filtering.
- Third, verify tenant-scoped cart and checkout behavior.

## Success Metrics

- At least two tenants have distinct starter catalogs.
- A shopper browsing one tenant cannot see another tenant’s products.
- A shopper can complete the first purchase flow within a selected tenant context.
- Local demo setup can seed or load tenant-owned catalog data repeatably.