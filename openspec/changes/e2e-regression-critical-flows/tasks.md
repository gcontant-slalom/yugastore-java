## 1. Availability And Storefront Smoke Coverage

- [x] 1.1 Automate the supported availability checks for the full application entrypoints required by the smoke scope.
- [x] 1.2 Automate storefront browsing assertions against the supported seeded catalog state.

## 2. Merchant Onboarding And Tenant Flow Coverage

- [x] 2.1 Automate the currently supported merchant onboarding or tenant-context journey already demonstrable in the repository.
- [x] 2.2 Verify logout, re-entry, and tenant visibility behavior for that supported flow.

## 3. Cart And Checkout Coverage

- [x] 3.1 Automate the supported add-to-cart and checkout journey for seeded products.
- [x] 3.2 Add assertions that fail explicitly on cart, routing, or checkout regressions in that journey.

## 4. Scope Stabilization

- [x] 4.1 Tag critical journeys into documented smoke or full-suite scope membership.
- [x] 4.2 Document known exclusions for incomplete or unsupported flows without weakening supported regression coverage.

Validation note: direct Playwright execution passed for the new smoke and full-browser journeys, but the supported `npm run smoke` and `npm run full` wrappers remain blocked in this environment because the harness bootstrap cannot find a runnable Java 17+ runtime on the host shell.
