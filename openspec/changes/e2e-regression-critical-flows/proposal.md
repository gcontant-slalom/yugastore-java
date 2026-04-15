## Why

Once the regression harness exists, Yugastore still needs coverage for the highest-value user journeys that prove the current application state has not regressed. Without those journey checks, the foundation would only verify that the environment can start, not that the application still works.

Derived from [resources/prds/yugastore-end-to-end-regression-suite.md](resources/prds/yugastore-end-to-end-regression-suite.md).

## What Changes

- Automate the current supported availability and storefront smoke journeys.
- Automate the supported merchant onboarding or tenant-context journey that is already demonstrable in the repository.
- Automate the current supported cart and checkout regression path.
- Organize these journeys into stable regression scopes that depend on the shared foundation harness.
- Keep unsupported or incomplete flows out of scope rather than inventing behavior.

## Capabilities

### New Capabilities
- `regression-critical-flows`: Automated end-to-end coverage for the supported application journeys that provide meaningful regression protection.

### Modified Capabilities
- None.

## Impact

- Root-level end-to-end regression workspace and fixtures
- `react-ui`
- `api-gateway-microservice`
- `products-microservice`
- `cart-microservice`
- `checkout-microservice`
- `login-microservice`
- Seed data and support assets under `resources/`
