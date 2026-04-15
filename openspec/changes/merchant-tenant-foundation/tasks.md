This change starts only after `merchant-auth-foundation` delivers a real authenticated user and session contract.

## 0. Auth Dependency Alignment

- [ ] 0.1 Confirm `merchant-auth-foundation` is complete and define how authenticated user identity maps into the tenant-context contract.

## 1. Merchant Onboarding Contract

- [ ] 1.1 Define the canonical merchant signup route at `/{tenantSlug}/signup`, minimum onboarding fields, slug validation rules, and first-slice tenant creation contract.
- [ ] 1.2 Decide whether the first merchant-admin user is created in the same flow or as an immediate follow-up and document the resulting contract.
- [ ] 1.3 Define the onboarding success-message lifecycle so tenant-created confirmation is shown only for the current successful submission and clears on route exit, reload, and later return.

## 2. Tenant Context Contract

- [ ] 2.1 Define the merchant company, store, and request-context contract for the first slice and map current demo data to a default merchant context.
- [ ] 2.2 Add canonical path-based tenant routing for `/`, `/{tenantSlug}/`, and `/{tenantSlug}/signup` in `react-ui` and `api-gateway-microservice`.
- [ ] 2.3 Identify the targeted request path and replace remaining hard-coded demo-user assumptions with auth-derived tenant context.
- [ ] 2.4 Add explicit invalid-slug and unknown-tenant handling so tenant routes do not silently fall back to the shared root storefront.

## 3. Persistence Foundation

- [ ] 3.1 Update YCQL schema and sample-data assets under `resources/` for tenant ownership fields needed by `products-microservice` and `checkout-microservice`.
- [ ] 3.2 Update any required YSQL-backed model or contract definitions without inventing a full auth flow in `login-microservice`.

## 4. Service Propagation

- [ ] 4.1 Implement tenant-context propagation from `api-gateway-microservice` to `products-microservice` for the first bounded flow.
- [ ] 4.2 Implement tenant-context propagation to `checkout-microservice` and persist ownership on targeted writes.
- [ ] 4.3 Add explicit missing-context rejection behavior for merchant-owned endpoints touched by this change.

## 5. Verification

- [ ] 5.1 Add focused tests for merchant onboarding routing, tenant-context propagation, and ownership persistence in the touched modules.
- [ ] 5.2 Add focused UI and regression coverage proving the merchant-tenant-created confirmation is absent on reload and after leaving then returning to the onboarding page.
- [ ] 5.3 Update developer-facing startup or sample-data guidance if the default merchant-context seed changes local verification behavior.