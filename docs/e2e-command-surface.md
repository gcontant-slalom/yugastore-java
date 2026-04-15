# End-to-End Command Surface

This document defines the supported end-to-end regression entrypoints that repository workflow guidance should reference.

## Supported Commands

Run all commands from the repository `e2e/` workspace unless the command itself says otherwise.

| Scope | Command | Use when | Notes |
| --- | --- | --- | --- |
| Smoke | `npm run smoke` | The changed behavior is covered by the smoke journeys in `docs/e2e-scope-matrix.md`. | Runs bootstrap, reset, baseline validation, and Playwright smoke-tagged specs. |
| Full | `npm run full` | The change touches a broader covered journey, multiple covered modules, or cross-service behavior. | Runs the same harness steps as smoke, then executes the full Playwright suite. |
| Bootstrap only | `npm run bootstrap` | You need to diagnose prerequisites, service reachability, or seeded baseline problems without mutating data. | Does not reset data. |
| Reset only | `npm run reset` | You need to recreate the documented baseline before rerunning a scope. | Reapplies schema and checked-in seed assets. |

## Selection Rules

- Use the narrowest supported regression scope that still covers the changed behavior.
- Use `smoke` for changes covered only by the availability or seeded storefront smoke journeys.
- Use `full` for cross-service or user-journey changes covered by merchant onboarding, cart, checkout, or multiple smoke plus full paths.
- If no supported end-to-end scope covers the change, say that explicitly and use the most relevant module-level verification instead of claiming end-to-end coverage.

## Current Coverage Inputs

- Journey-to-scope mapping lives in `docs/e2e-scope-matrix.md`.
- Harness prerequisites, reset behavior, and artifact retention live in `docs/e2e-testing.md`.

## Verification Reporting Expectations

When reporting results in agent output, use one of these patterns:

- Passed: name the scope or command that ran and state that it passed.
- Blocked: name the scope that should have run, what blocked it, and what alternative verification did run.
- Skipped: name the scope that was not run and explain why it was intentionally out of scope for the change.

Examples:

- `Regression: smoke passed via npm run smoke`
- `Regression blocked: full could not run because Java 17 was unavailable to the harness shell; direct Playwright suite passed instead`
- `Regression skipped: no supported end-to-end scope covers this docs-only change`

## Fallback Expectations

- If the supported wrapper command cannot run because of a documented prerequisite problem, report that blocker explicitly and do not imply success.
- If a narrower direct verification path exists and is materially useful, report it as fallback verification rather than as a replacement for the blocked supported scope.
- If nothing relevant can run, say so plainly and identify the missing dependency, service, runtime, or fixture.

## Current Known Wrapper Blocker

- In environments where the harness shell cannot resolve a runnable Java 17+ runtime, `npm run smoke` and `npm run full` fail during bootstrap before Playwright runs.
- In that case, direct Playwright execution may still be useful for browser-level checks, but it must be reported as fallback verification rather than as the supported harness scope.