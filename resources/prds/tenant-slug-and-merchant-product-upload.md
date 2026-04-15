# Tenant Slug Routing And Merchant Product Upload

## Problem Statement

Yugastore's current planning covers merchant signup, tenant context, tenant-filtered catalogs, and tenant-scoped purchase flows, but it does not fully specify the canonical tenant slug routes or any merchant-facing product upload capability. The result is a gap between the intended user journey and the current implementation contract: a merchant cannot yet rely on a stable store URL shape, cannot self-populate a tenant catalog, and the system does not yet define how a shopper reaches a tenant storefront through the exact path structure required for the demo.

## Goals

- Define one canonical local tenant slug contract for the storefront root, tenant storefront, and tenant signup path.
- Preserve the original root storefront at `http://127.0.0.1:8080` while supporting tenant storefronts at `http://127.0.0.1:8080/{tenantSlug}/`.
- Support merchant signup at `http://127.0.0.1:8080/{tenantSlug}/signup`.
- Ensure the tenant slug resolves the active tenant context across frontend, gateway, and downstream storefront or checkout calls.
- Allow a merchant-admin user to upload products into the merchant's own tenant catalog.
- Ensure a separate shopper can browse only the products belonging to the tenant identified by the slug and purchase those products.

## Non-Goals

- Custom-domain or subdomain routing.
- Merchant analytics, dashboards, theming, or fraud tooling.
- Cross-tenant carts or mixed-store checkout.
- Rich merchant catalog management beyond the first upload capability.
- Payment processor onboarding or merchant payout configuration.

## Background / Context

This PRD is derived from the tenant and catalog gap analysis in this chat and the existing repository PRDs and OpenSpec changes for merchant onboarding and tenant-owned catalogs.

The current OpenSpec work already covers:

- user registration, login, and logout
- merchant tenant creation and path-based tenant routing
- tenant-filtered storefront browsing
- tenant-scoped purchase behavior

The current gaps are:

- the slug contract is not pinned to one canonical route structure
- the exact root, tenant storefront, and tenant signup paths are not specified
- merchant product upload is explicitly deferred and therefore missing from the current execution plan

This PRD makes the slug contract explicit and introduces a first merchant product upload slice that remains bounded enough for the current brownfield stack.

## Architecture / Diagram References

- [resources/prds/diagrams/tenant-slug-storefront-context.mmd](diagrams/tenant-slug-storefront-context.mmd)
- [resources/prds/diagrams/tenant-slug-onboarding-sequence.mmd](diagrams/tenant-slug-onboarding-sequence.mmd)

## Personas

- Merchant admin who needs to create a tenant-specific store and load products into it.
- Shopper who needs to access a tenant storefront by URL and buy products from that store only.
- Delivery team member who needs a deterministic localhost route structure for demo and test automation.

## User Stories

- As a merchant admin
  I want my store to live at a tenant-specific slug path
  So that I can share a stable URL for my storefront.

- As a merchant admin
  I want to sign up for my store at a slug-specific signup URL
  So that onboarding is visibly tied to my tenant.

- As a merchant admin
  I want to upload products into my tenant catalog
  So that my store can show my own products instead of seeded demo data only.

- As a shopper
  I want to open a tenant storefront URL and see only that store's products
  So that I shop within the intended store context.

- As a shopper
  I want to purchase products from that tenant storefront
  So that the end-to-end store journey works for a real tenant path.

## Functional Requirements

- The system must preserve the original demo storefront root at `http://127.0.0.1:8080`.
- The system must support a canonical tenant storefront route at `http://127.0.0.1:8080/{tenantSlug}/`.
- The system must support a canonical tenant signup route at `http://127.0.0.1:8080/{tenantSlug}/signup`.
- The system must treat the tenant slug in the browser path as the authoritative route input for tenant resolution in the supported storefront flow, while `/` remains the demo store.
- The system must create or reserve the requested tenant slug during merchant signup and reject duplicate or invalid slugs clearly.
- Tenant slug resolution must not silently fall back to the shared root storefront when a tenant slug is unknown.
- Merchant signup must create a merchant-admin account and a tenant bound to the requested slug in one supported flow.
- The system must allow an authenticated merchant-admin user to upload products into the merchant's own tenant catalog.
- Uploaded product records must persist tenant ownership derived from the active tenant slug and merchant association.
- The first upload capability must be create-only and must reject rows that reuse an existing product key for the same tenant.
- A shopper opening a tenant storefront route must see only the products owned by that tenant.
- A shopper purchase originating from a tenant storefront route must remain scoped to that tenant through checkout.
- The first upload capability must return clear feedback for accepted rows, rejected rows, and tenant-ownership validation failures.

## Non-Functional Requirements

- The canonical route structure must work locally without DNS, proxy, or host-file changes.
- Slug handling must be deterministic across frontend routing, gateway routing, and downstream tenant-context propagation.
- Invalid or duplicate slug outcomes must be explicit and user-visible.
- The first upload slice should align with existing checked-in data conventions so the brownfield repository does not need a broad ingestion platform redesign.
- The first implementation should be simple enough to support repeatable local demo and automated regression coverage.

## Constraints

- Existing merchant tenant and catalog changes are already active and should not be contradicted.
- The repository currently uses Spring Boot microservices with React UI routing through the API gateway.
- Product and checkout persistence is YCQL-backed in this repository.
- Product upload must fit a bounded first slice rather than a full merchant back office.

## Assumptions

- Tenant slug format for the first slice should be lowercase kebab-case, unique within the platform, and stable after creation.
- The first merchant signup flow creates the first merchant-admin user and the tenant in a single flow.
- The first product upload slice should use CSV upload because the repository already uses checked-in CSV product assets and that keeps the first ingestion path small.
- The first product upload slice is create-only; edit behavior will be specified in a later change.
- Shopper browsing may remain unauthenticated if the existing storefront allows it, but any protected purchase behavior must still preserve tenant context.

## Open Questions

- What are the minimum merchant signup fields beyond email, password, store name, and requested slug?
- Should a tenant slug ever be editable after creation, or remain immutable for the first slice?
- Which exact CSV columns are mandatory for the first upload contract?
- None for root path behavior. `/` remains the demo store.

## Risks

- If slug routing rules differ between React UI and gateway handling, merchants or shoppers may land in the wrong tenant context.
- If upload ownership checks are weak, merchants could upload products into the wrong tenant.
- If demo-store behavior at `/` drifts toward tenant fallback behavior, regression tests may normalize unintended routing errors.
- If upload scope expands into full merchant catalog administration, the slice may become too large for the current codebase state.

## Dependencies

- [resources/prds/merchant-tenant-foundation.md](merchant-tenant-foundation.md)
- [resources/prds/merchant-catalog-bootstrap.md](merchant-catalog-bootstrap.md)
- Existing auth foundation and current tenant-context propagation work
- Agreement between `react-ui`, `api-gateway-microservice`, `login-microservice`, and `products-microservice` on slug and ownership contracts

## Candidate OpenSpec Changes

- `merchant-tenant-foundation`: refine the tenant-route contract to the canonical slug paths and slug-validation rules.
- `merchant-product-upload-foundation`: add the first merchant-admin product upload capability with tenant-bound ownership.

## OpenSpec Change Dependencies

- `merchant-tenant-foundation` remains upstream of `merchant-product-upload-foundation` for tenant creation and slug resolution.
- `merchant-product-upload-foundation` should align with `merchant-catalog-bootstrap` so uploaded products and seeded products share the same ownership model.

## Recommended Sequencing

- First, tighten the existing tenant foundation change to the exact canonical routes: `/`, `/{tenantSlug}/`, and `/{tenantSlug}/signup`.
- Second, enforce slug validation, uniqueness, and explicit invalid-tenant handling.
- Third, add merchant product upload bound to the authenticated merchant tenant.
- Fourth, extend storefront and checkout verification to prove that uploaded tenant-owned products are visible and purchasable only through the matching slug route.

## Success Metrics

- A merchant can sign up through `http://127.0.0.1:8080/my-test-store/signup` and receive the `my-test-store` tenant.
- Opening `http://127.0.0.1:8080/my-test-store/` shows only that tenant's products.
- A merchant can upload products into `my-test-store` and those products appear only in that store.
- A separate shopper can purchase a product from `http://127.0.0.1:8080/my-test-store/` without crossing tenant boundaries.
- Unknown or duplicate slugs are rejected explicitly rather than silently routed to the shared root storefront.