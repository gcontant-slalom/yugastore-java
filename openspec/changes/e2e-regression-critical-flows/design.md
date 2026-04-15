## Context

The repository already exposes a few manually verified journeys, including storefront access and merchant onboarding or tenant-list behavior, but those checks are ad hoc and not preserved as repeatable tests. This change turns the supported current-state journeys into automated regression coverage on top of the shared harness.

## Goals / Non-Goals

**Goals:**
- Cover the core journeys that best prove the application still works end to end.
- Keep each automated journey aligned to behavior the repository already supports.
- Structure the journey suite so smoke checks and broader regressions can share the same harness and fixtures.

**Non-Goals:**
- Cover every endpoint or UI branch.
- Encode unsupported login behavior or speculative future merchant workflows.
- Replace focused service-level tests where those are still the better verification tool.

## Decisions

### 1. Prioritize supported happy paths over exhaustive matrices

Recommended decision: automate a narrow set of high-value happy paths first.

- Why: the user goal is regression protection for the current state, not exhaustive combinatorics.
- Alternative considered: broad edge-case coverage in the first slice.
- Rejected because it would slow delivery before baseline protection exists.

### 2. Use browser-first journeys with API-assisted setup only when necessary

Recommended decision: keep user-visible flows browser-driven, but allow API-assisted setup when it improves determinism without reducing coverage of the target journey.

- Why: browser journeys prove the integrated app, while API setup can keep the suite stable.
- Alternative considered: pure browser setup for every prerequisite.
- Rejected because it would duplicate slow setup steps and increase brittleness.

### 3. Treat incomplete login behavior conservatively

Recommended decision: only automate login and merchant flows that are already supported and demonstrable in the repository.

- Why: the repo instructions explicitly warn that `login-microservice` is incomplete.
- Alternative considered: backfill or assume missing identity behavior in tests.
- Rejected because regression tests must protect actual current behavior, not aspirational flows.

### 4. Organize tests by user journey and scope membership

Recommended decision: group tests by user journey and tag them for smoke or full regression membership.

- Why: the workflow needs fast default checks and an understandable mapping from failures to product areas.
- Alternative considered: organize only by page or service.
- Rejected because regressions are easier to reason about in end-user journey units.

## Risks / Trade-offs

- [Journey data becomes brittle] -> Keep fixtures deterministic and reuse the foundation reset behavior.
- [Coverage overreaches into unsupported behavior] -> Limit requirements to current demonstrable flows and explicitly document exclusions.
- [Failures are hard to localize] -> Align journey names and tags to product areas and scope definitions.

## Migration Plan

- Add smoke coverage for availability and storefront access first.
- Add the supported merchant onboarding or tenant-context path next.
- Add the current cart and checkout path after fixtures are stable.
- Stabilize scope membership and known limitations after the core journeys are automated.

## Open Questions

- Which exact merchant onboarding path should remain the canonical regression journey if that area changes during implementation.
- Whether the first cart and checkout regression should be browser-only or use API setup to reach a deterministic cart state faster.
