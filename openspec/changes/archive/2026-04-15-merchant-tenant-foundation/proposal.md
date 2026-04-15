## Why

Yugastore still needs tenant and merchant context for the broader white-label roadmap, but authentication is now the first prerequisite. The requirements transcript also makes the next slice concrete: a salesperson should be able to share a company signup link, onboarding should create a merchant tenant, and local demos can use path-based tenant routing instead of custom domains.

Derived from [resources/prds/merchant-tenant-foundation.md](resources/prds/merchant-tenant-foundation.md).

## What Changes

- Introduce explicit merchant company and store context as a post-auth product slice.
- Add a merchant company signup entry point at a tenant-specific path that creates tenant context from a shared onboarding link.
- Resolve tenant context from a canonical browser-visible slug path that works for local demos.
- Treat the merchant-tenant-created success confirmation as transient UI state that is shown only immediately after a successful onboarding submission.
- Preserve the shared storefront root at `/` while defining tenant storefront routes at `/{tenantSlug}/` and merchant signup routes at `/{tenantSlug}/signup`.
- Validate requested tenant slugs for uniqueness and supported format before tenant creation succeeds.
- Replace targeted hard-coded demo-user assumptions with a request-context contract derived from authenticated user identity.
- Persist tenant ownership on targeted merchant-owned records touched by the first slice.
- Depend on completed `merchant-auth-foundation` work before this change begins.
- Defer reporting, broad theming, and later white-label work beyond this post-auth foundation.

## Capabilities

### New Capabilities
- `merchant-tenant-context`: Merchant onboarding, tenant creation, path-based tenant routing, and request-context foundations for the initial white-label slice.

### Modified Capabilities
- None.

## Impact

- `api-gateway-microservice`
- `react-ui`
- `products-microservice`
- `checkout-microservice`
- Potential supporting model or contract updates affecting `cart-microservice` and `login-microservice`
- Schema and sample-data assets under `resources/`
- Request and ownership contracts across YCQL and YSQL-backed services after auth identity is available