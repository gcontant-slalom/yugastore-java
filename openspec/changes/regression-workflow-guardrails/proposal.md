## Why

Regression tests only prevent breakage if maintainers and Copilot actually run them and if test updates cannot quietly normalize regressions. Yugastore therefore needs explicit workflow guardrails for when regression checks must run and how regression tests may be changed.

Derived from [resources/prds/yugastore-end-to-end-regression-suite.md](resources/prds/yugastore-end-to-end-regression-suite.md).

## What Changes

- Update repository workflow guidance so Copilot runs the relevant regression commands when covered areas change.
- Define repository policy for blocked or skipped regression execution.
- Require explicit rationale and confirmation before regression tests are relaxed, removed, or repurposed.
- Keep the workflow guardrails tied to the stable command surface provided by the regression foundation.

## Capabilities

### New Capabilities
- `regression-workflow-governance`: Copilot workflow expectations, regression-run reporting, and rules for modifying regression tests safely.

### Modified Capabilities
- None.

## Impact

- `.github/copilot-instructions.md`
- Repository-local workflow guidance tied to end-to-end verification
- Future regression test files and change-review expectations
