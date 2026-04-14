This change starts only after `merchant-auth-foundation` delivers a real authenticated user and session contract.

## 0. Auth Dependency Alignment

- [ ] 0.1 Confirm `merchant-auth-foundation` is complete and define how authenticated user identity maps into the tenant-context contract.

## 1. Tenant Context Contract

- [ ] 1.1 Define the merchant company, store, and request-context contract for the first slice and map current demo data to a default merchant context.
- [ ] 1.2 Identify the targeted request path and replace remaining hard-coded demo-user assumptions with auth-derived tenant context.

## 2. Persistence Foundation

- [ ] 2.1 Update YCQL schema and sample-data assets under `resources/` for tenant ownership fields needed by `products-microservice` and `checkout-microservice`.
- [ ] 2.2 Update any required YSQL-backed model or contract definitions without inventing a full auth flow in `login-microservice`.

## 3. Service Propagation

- [ ] 3.1 Implement tenant-context propagation from `api-gateway-microservice` to `products-microservice` for the first bounded flow.
- [ ] 3.2 Implement tenant-context propagation to `checkout-microservice` and persist ownership on targeted writes.
- [ ] 3.3 Add explicit missing-context rejection behavior for merchant-owned endpoints touched by this change.

## 4. Verification

- [ ] 4.1 Add focused tests for tenant-context propagation and ownership persistence in the touched modules.
- [ ] 4.2 Update developer-facing startup or sample-data guidance if the default merchant-context seed changes local verification behavior.