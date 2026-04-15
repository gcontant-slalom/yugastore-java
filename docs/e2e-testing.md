# End-to-End Regression Harness

This document defines the supported local command surface for the Yugastore end-to-end regression foundation introduced by issue #24.

## Scope

- `npm run smoke` in `e2e/`: resets the supported baseline, validates the stack prerequisites, and runs Playwright specs tagged with `@smoke` when they exist.
- `npm run full` in `e2e/`: performs the same reset and bootstrap flow, then runs the broader Playwright suite when specs exist.
- `npm run bootstrap` in `e2e/`: validates runtime prerequisites, required services, and the seeded baseline without changing data.
- `npm run reset` in `e2e/`: recreates the supported YCQL and YSQL baseline from the checked-in schema and CSV assets.

## Prerequisites

- Java 17 or newer on `PATH`
- Node 18 or newer plus `npm`
- E2E dependencies installed from the root-level workspace:

```bash
cd e2e
npm install --package-lock=false
```

- A supported YugabyteDB runtime:
  - Preferred: Docker container named `yugastore-yb`
  - Alternative: local `ycqlsh` plus local `ysqlsh`
- The required application services started locally:
  - `eureka-server-local` on `8761`
  - `api-gateway-microservice` on `8081`
  - `products-microservice` on `8082`
  - `cart-microservice` on `8083`
  - `checkout-microservice` on `8086`
  - `react-ui` on `8080`

The harness treats `login-microservice` on `8085` as optional because the repository still documents it as incomplete.

## Supported Workflow

1. Start YugabyteDB with either the host-install path or the `yugastore-yb` Docker container described in the repository runbook.
2. Start the required services in the documented order, or use the existing Docker image helper where appropriate.
3. Install the `e2e/` workspace dependencies once.
4. Run `npm run smoke` for routine local validation.
5. Run `npm run full` when you need the broader supported scope.

Both scope commands follow the same sequence:

1. Validate runtime prerequisites and required service reachability.
2. Reset YCQL and YSQL state from `resources/schema.cql`, `resources/schema.sql`, and the checked-in seed CSV files.
3. Re-validate the seeded baseline counts.
4. Run the configured Playwright scope.
5. Preserve the run summary and Playwright artifacts under `.tmp/e2e/`.

## Deterministic Baseline

The reset path currently guarantees the following baseline before Playwright runs:

- `cronos.products` matches `resources/cronos_products.csv`
- `cronos.product_rankings` matches `resources/cronos_product_rankings.csv`
- `cronos.product_inventory` matches `resources/cronos_product_inventory.csv`
- `shopping_cart` is truncated to zero rows

If `npm run bootstrap` reports a baseline mismatch, rerun `npm run reset` and then rerun the scope. The command surface is intentionally strict so repeated executions start from the same documented data state.

## Diagnostics

- Every run writes a timestamped summary under `.tmp/e2e/<scope>/<run-id>/summary.json` and `.tmp/e2e/<scope>/<run-id>/summary.txt`.
- Playwright outputs go to `.tmp/e2e/<scope>/<run-id>/playwright/`.
- On assertion or browser failures, Playwright retains traces, screenshots, and video according to `e2e/playwright.config.cjs`.
- On startup or data failures, the summary file records which prerequisite or baseline check blocked execution.

## Current Foundation Limits

- Issue #24 defines the harness only. The stable command surface is available immediately, but critical-flow specs are intentionally deferred to issue #25.
- The Playwright commands use `--pass-with-no-tests` for now so the supported smoke and full entrypoints remain runnable before journey specs land.
- If the machine does not expose either the `yugastore-yb` container or a local `ycqlsh` plus `ysqlsh` pair, reset and bootstrap will fail fast with an actionable message.