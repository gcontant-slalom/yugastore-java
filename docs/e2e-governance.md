# End-to-End Governance

This document defines repository rules for modifying end-to-end regression tests and related workflow guidance.

## Protected Regression Assets

Treat the following as protected regression assets:

- `e2e/tests/**`
- `e2e/page-objects/**`
- workflow guidance that tells maintainers when or how to run supported regression scopes
- regression scope mapping and command-surface documentation under `docs/e2e-*.md`

## Allowed Changes

Regression tests and workflow guidance may be updated when at least one of these is true:

- the product behavior intentionally changed and the regression suite must reflect the new supported contract
- the previous regression test or assertion was incorrect
- the regression scope needs to be re-scoped for a documented reason without weakening the covered contract silently
- the workflow guidance was incomplete or inaccurate relative to the supported command surface

## Required Rationale

Any change to an existing regression test or protected workflow guidance must say which of the following applies:

- product behavior changed
- previous test was incorrect
- test or workflow guidance needed to be re-scoped for a documented reason

The rationale must be stated in the issue, PR description, or final agent report. Do not rely on the diff alone to carry that meaning.

## Explicit Confirmation Rule

Do not relax, remove, or materially repurpose an existing regression assertion or journey unless one of the following provides explicit confirmation:

- the user directly requested or approved that weakening
- the backing GitHub issue explicitly authorizes the change

Absent that confirmation, preserve or strengthen the regression coverage instead of narrowing it.

## Prohibited Silent Weakening

The following are not allowed without the rationale and confirmation above:

- deleting a regression journey because it is failing
- reducing assertions just to make a failing test pass
- moving a protected regression from smoke or full coverage to no coverage without documenting why
- repurposing an existing regression journey to test a different behavior while silently dropping the original contract

## Blocked Verification Reporting

When regression verification cannot run fully, the final report must include:

- the scope that should have run
- what blocker prevented it from running
- any fallback verification that did run
- what remained unverified because of the blocker

Do not phrase blocked verification as a pass.