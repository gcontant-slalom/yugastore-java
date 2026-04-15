# End-to-End Scope Matrix

This matrix defines the supported journey-to-scope mapping for the critical regression coverage introduced by issue #25.

## Supported Journeys

| Journey | Spec coverage | Smoke | Full | Notes |
| --- | --- | --- | --- | --- |
| Stack availability and routed catalog entrypoints | `e2e/tests/availability-and-storefront.spec.js` | Yes | Yes | Confirms the React UI, Eureka dashboard, and routed product catalog are reachable before deeper journeys run. |
| Seeded storefront browsing | `e2e/tests/availability-and-storefront.spec.js` | Yes | Yes | Verifies the current seeded Books storefront can load and open a product details page. |
| Merchant onboarding and tenant visibility | `e2e/tests/merchant-onboarding.spec.js` | No | Yes | Covers the currently supported browser-visible merchant setup path at `/merchant/signup`, including logout and login re-entry. |
| Authenticated cart and checkout | `e2e/tests/cart-checkout.spec.js` | No | Yes | Uses a freshly registered user to add a seeded product to the cart and complete checkout. |

## Scope Intent

- `smoke`: fast, repeatable checks for basic stack reachability and seeded storefront visibility.
- `full`: smoke coverage plus the authenticated merchant and shopper journeys that mutate user-specific state.

## Known Exclusions

- Canonical tenant-slug storefront and signup routes are excluded until the reserved-path work in issue `#12` lands. The current supported merchant regression path remains `/merchant/signup`.
- Product-details-page assertions are excluded from the current smoke journey because the brownfield `/item/:asin` route is not yet stable enough to treat as supported regression coverage in this slice.
- The suite covers only the auth behavior already exposed through the React UI: register, login, logout, current-user session restore, and merchant setup. It does not attempt unsupported login edge cases, password recovery, or broader account administration.
- Merchant product upload, tenant-isolated storefront catalogs, and tenant-scoped checkout are excluded because those behaviors belong to later OpenSpec changes and issues `#28` through `#32`.
- The smoke scope intentionally excludes merchant onboarding and checkout because those flows require mutable user records and take longer to execute than the baseline storefront checks.