## Why

Yugastore's current tenant catalog plan intentionally stops at seed data, which leaves a critical gap in the target user journey: a merchant can create a tenant but still cannot load products into that tenant through the product itself. The next bounded slice is therefore the first merchant-admin upload capability, built on top of canonical slug-based tenant routing and tenant-owned catalog persistence.

Derived from [resources/prds/tenant-slug-and-merchant-product-upload.md](resources/prds/tenant-slug-and-merchant-product-upload.md).

## What Changes

- Add a first-slice merchant-admin product upload capability for a tenant-owned catalog.
- Bind upload requests to the authenticated merchant and active tenant slug instead of relying on seed-only catalog setup.
- Validate uploaded product rows and return explicit accepted-row and rejected-row results.
- Persist tenant ownership on uploaded product and targeted inventory records.
- Make uploaded tenant-owned products visible only through the matching tenant storefront and tenant-scoped purchase flow.
- Keep the first upload slice bounded to one supported file-based ingestion path instead of a full merchant catalog management surface.

## Capabilities

### New Capabilities
- `merchant-product-upload`: Merchant-admin product upload, validation feedback, and tenant-bound catalog ownership for the first self-serve catalog slice.

### Modified Capabilities
- None.

## Impact

- `react-ui`
- `api-gateway-microservice`
- `products-microservice`
- Potential supporting contract updates in `login-microservice`
- Product-related schema or seed asset conventions under `resources/`
- Storefront and checkout flows that consume tenant-owned product records