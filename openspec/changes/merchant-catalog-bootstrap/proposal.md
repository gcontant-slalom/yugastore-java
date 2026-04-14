## Why

The requirements transcript expects the demo to show at least two tenants with different products and a purchase flow, but it also explicitly allows the team to seed catalog data instead of building full merchant admin interfaces. Yugastore therefore needs one bounded change that makes tenant-owned catalogs visible and shoppable without pulling reporting, upload UI, or payment-gateway setup into the same slice.

Derived from [resources/prds/merchant-catalog-bootstrap.md](resources/prds/merchant-catalog-bootstrap.md).

## What Changes

- Add tenant-owned starter catalog support using checked-in seed assets for the first slice.
- Filter storefront product browsing by the active tenant context.
- Keep cart and checkout behavior scoped to the active tenant for the first demonstrable flow.
- Extend existing sample-data and ownership contracts instead of building a full merchant back office.
- Defer merchant upload UI, low-stock alerts, dashboards, reporting, and fraud tooling.

## Capabilities

### New Capabilities
- `merchant-catalog-bootstrap`: Tenant-owned starter catalogs, tenant-filtered storefront browsing, and tenant-scoped shopping behavior.

### Modified Capabilities
- None.

## Impact

- `products-microservice`
- `checkout-microservice`
- `cart-microservice`
- `api-gateway-microservice`
- `react-ui`
- Sample-data and schema assets under `resources/`