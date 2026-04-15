## Context

The repository already documents how to start YugabyteDB, seed data, and launch services, but those steps are still human-operated and fragmented. The regression foundation needs to turn that runbook into a single supported harness that later flow coverage can reuse without duplicating startup or fixture logic in every test.

## Goals / Non-Goals

**Goals:**
- Provide one supported end-to-end harness entrypoint for local regression execution.
- Centralize prerequisite checks, startup assumptions, and deterministic data reset behavior.
- Standardize smoke and full-suite command names so workflow automation can refer to them consistently.
- Preserve failure artifacts and summaries that make cross-service regressions diagnosable.

**Non-Goals:**
- Automate all business journeys in this change.
- Replace targeted unit or module-level tests.
- Decide CI rollout beyond defining a stable local command surface.

## Decisions

### 1. Use a dedicated root-level regression workspace

Recommended decision: keep the end-to-end harness in a root-level workspace rather than embedding it inside a single service or the React frontend.

- Why: the suite spans browser, gateway, service orchestration, and data reset concerns that do not belong to one module.
- Alternative considered: place the runner inside `react-ui/frontend`.
- Rejected because the bootstrap logic and failure diagnostics are cross-service concerns.

### 2. Wrap existing startup and seed flows behind one supported bootstrap interface

Recommended decision: standardize one bootstrap layer that reuses the documented startup order and seed assets instead of rewriting the environment model from scratch.

- Why: the repository already has a working runbook and checked-in data assets.
- Alternative considered: introduce a fully separate environment topology just for tests.
- Rejected because it would create a second operational model to maintain.

### 3. Separate bootstrap, execution, and artifact collection responsibilities

Recommended decision: structure the harness so environment preparation, test execution, and failure reporting are distinct steps under one command surface.

- Why: startup failures, data failures, and assertion failures need different diagnostics.
- Alternative considered: one opaque script with mixed responsibilities.
- Rejected because it would make failures harder to triage.

### 4. Publish smoke and full-suite scopes from the start

Recommended decision: define at least a fast smoke scope and a broader full regression scope in the foundation.

- Why: workflow guardrails need a stable target for “run the relevant regression checks,” and not every change can afford the full suite.
- Alternative considered: define only one monolithic suite.
- Rejected because it would either be too slow for routine development or too narrow to protect the application.

## Risks / Trade-offs

- [Bootstrap path becomes flaky] -> Reuse existing documented assets and add explicit preflight checks before tests run.
- [Smoke and full scopes drift] -> Tag and document scope membership centrally in the harness.
- [Cross-service failures produce poor diagnostics] -> Preserve traces, screenshots, and service-log pointers as part of the reporting contract.
- [Environment ownership remains unclear] -> Keep bootstrap steps explicit and repository-local instead of relying on undocumented machine state.

## Migration Plan

- Introduce the harness structure and command surface.
- Wrap the supported startup and seed workflow behind the harness bootstrap path.
- Validate the harness against the current full application stack.
- Hand off critical-journey automation to the dependent `e2e-regression-critical-flows` change.

## Open Questions

- Whether the bootstrap should prefer `docker-run.sh` or manual service startup as the default implementation path.
- What maximum runtime should separate the smoke scope from the full scope.
