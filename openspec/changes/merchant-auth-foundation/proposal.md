## Why

Yugastore cannot support real user access today because authentication is stubbed across the brownfield stack. The requirements transcript explicitly calls out fixing login first, and the current codebase confirms that registration, login, logout, frontend auth state, gateway enforcement, and hard-coded demo-user behavior are incomplete or inconsistent.

Derived from [resources/prds/merchant-auth-foundation.md](resources/prds/merchant-auth-foundation.md).

## What Changes

- Add the minimum complete authentication capability for email/password registration, login, and logout.
- Make email the public credential identifier for the first auth slice.
- Establish one consistent authenticated-state contract across `react-ui`, `api-gateway-microservice`, and `login-microservice`.
- Remove hard-coded demo-user reliance from the auth-critical request path.
- Defer tenant or company modeling, onboarding, advanced authorization, MFA, SSO, password reset, and other downstream features until auth is complete.

## Capabilities

### New Capabilities
- `basic-user-authentication`: Email/password account creation, login, logout, and authenticated-state handling for the brownfield app.

### Modified Capabilities
- None.

## Impact

- `login-microservice`
- `api-gateway-microservice`
- `react-ui`
- Potential supporting identity-contract updates affecting `cart-microservice` and `checkout-microservice`
- YSQL schema and seed assets under `resources/`
- Browser, gateway, and service auth-state handling for user-facing flows