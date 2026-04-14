## Why

Yugastore still needs tenant and merchant context for the broader white-label roadmap, but authentication is now the first prerequisite. This change is therefore sequenced after `merchant-auth-foundation`, once the application has a real authenticated user and session contract instead of a stub.

Derived from [resources/prds/white-label-multitenant-yugastore.md](resources/prds/white-label-multitenant-yugastore.md).

## What Changes

- Introduce explicit merchant company and store context as a post-auth product slice.
- Replace targeted hard-coded demo-user assumptions with a request-context contract derived from authenticated user identity.
- Persist tenant ownership on targeted merchant-owned records touched by the first slice.
- Depend on completed `merchant-auth-foundation` work before this change begins.
- Defer reporting, broad theming, and later white-label work beyond this post-auth foundation.

## Capabilities

### New Capabilities
- `merchant-tenant-context`: Tenant, store, and request-context foundations for merchant-owned operations in the initial white-label slice.

### Modified Capabilities
- None.

## Impact

- `api-gateway-microservice`
- `products-microservice`
- `checkout-microservice`
- Potential supporting model or contract updates affecting `cart-microservice`, `login-microservice`, and `react-ui`
- Schema and sample-data assets under `resources/`
- Request and ownership contracts across YCQL and YSQL-backed services after auth identity is available