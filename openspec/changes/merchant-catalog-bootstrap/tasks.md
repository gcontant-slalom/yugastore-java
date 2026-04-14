This change starts only after `merchant-tenant-foundation` provides tenant creation and tenant-context routing.

## 1. Seed and Ownership Foundation

- [ ] 1.1 Define the tenant-owned starter catalog data shape and choose the canonical seed asset workflow under `resources/`.
- [ ] 1.2 Extend product-related schema or seed assets so at least two tenants have distinct starter catalogs.

## 2. Storefront Tenant Filtering

- [ ] 2.1 Update `api-gateway-microservice` and `react-ui` so the active tenant route drives storefront catalog requests.
- [ ] 2.2 Update `products-microservice` to filter the first storefront catalog flow by tenant ownership.

## 3. Tenant-Scoped Shopping Flow

- [ ] 3.1 Update the targeted `cart-microservice` or `checkout-microservice` path so the first shopping flow carries active tenant context.
- [ ] 3.2 Persist tenant ownership on the targeted shopping records created by the first purchase flow.
- [ ] 3.3 Reject shopping requests that do not include resolvable tenant context.

## 4. Verification and Demo Readiness

- [ ] 4.1 Add focused tests for tenant-owned catalog filtering and the first tenant-scoped shopping path.
- [ ] 4.2 Update local demo or sample-data guidance so two tenant catalogs can be loaded and demonstrated repeatably.