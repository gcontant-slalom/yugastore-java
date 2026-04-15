## Why

Yugastore does not yet have one supported, repeatable end-to-end regression harness that proves the full application can start, seed data, and execute covered journeys without manual assembly. The repository needs that foundation before critical-path browser flows and workflow enforcement can become reliable.

Derived from [resources/prds/yugastore-end-to-end-regression-suite.md](resources/prds/yugastore-end-to-end-regression-suite.md).

## What Changes

- Define the supported end-to-end regression harness for local whole-application validation.
- Establish a deterministic bootstrap path for prerequisites, seeded data, and environment checks.
- Standardize the regression command surface for at least smoke and full-suite execution.
- Produce actionable pass or fail output with preserved failure artifacts.
- Keep the harness independent from later journey coverage so critical flows can be layered on top cleanly.

## Capabilities

### New Capabilities
- `regression-harness`: End-to-end bootstrap, fixture reset, command surface, and reporting for full-application regression execution.

### Modified Capabilities
- None.

## Impact

- Root-level regression automation files and scripts
- Seed and startup assets under `resources/`
- Local verification workflow for the full application stack
- Future browser and API regression suites that will depend on this harness
