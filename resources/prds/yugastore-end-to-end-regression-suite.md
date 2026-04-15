# Yugastore End-to-End Regression Suite

## Problem Statement

Yugastore currently lacks a single, repeatable regression test suite that exercises the application end to end across service startup, seeded data, browser flows, and cross-service API behavior. That leaves the repository exposed to silent regressions, especially when changes span the gateway, storefront, tenant onboarding, cart, checkout, or service orchestration.

## Goals

- Establish a deterministic end-to-end regression harness that can be run locally against the full application stack.
- Cover the highest-value user journeys that prove the current application still works after changes.
- Make regression execution part of the expected Copilot workflow when touched code intersects covered journeys.
- Require an explicit explanation and confirmation whenever existing regression tests must be modified because behavior intentionally changed.
- Produce implementation slices that can be tracked through OpenSpec and GitHub issues.

## Non-Goals

- Exhaustive edge-case automation for every endpoint and UI branch in the first delivery.
- Full replacement of focused unit, slice, or service-level tests already owned by individual modules.
- Broad load, soak, or security testing.
- Inventing unsupported login or identity flows beyond what the repository currently implements.

## Background / Context

This PRD is derived from the current repository state and the planning request captured in this chat. The repository is a Spring Boot microservice sample with a React storefront wrapper, Eureka-based discovery, and a YugabyteDB split between YCQL-backed and YSQL-backed services. The current automated coverage in the repository is mostly narrow and does not provide one command or one workflow that proves the full application remains healthy after change.

The current startup runbook already documents how to bring up YugabyteDB, seed data, and launch the services. Recent manual checks in this workspace also show that some merchant onboarding and tenant-context flows have been exercised through ad hoc curl commands. Those facts make a formalized regression suite practical: the system already has repeatable data sources, a known startup order, and identifiable critical journeys.

## Architecture / Diagram References

- [resources/prds/diagrams/yugastore-e2e-regression-flow.mmd](diagrams/yugastore-e2e-regression-flow.mmd)

## Personas

- Maintainer who needs a reliable signal that a cross-service change did not break the application.
- Copilot-assisted developer who needs a clear workflow for which regression checks must run before finishing work.
- Reviewer who needs confidence that modified regression tests reflect an intentional product change rather than accidental drift.

## User Stories

- As a maintainer
  I want one documented end-to-end regression workflow
  So that I can verify the current application state before and after changes.

- As a developer using Copilot
  I want the repository instructions to tell the agent when to run regression checks
  So that end-to-end validation becomes part of normal implementation work.

- As a reviewer
  I want any regression test changes to come with a clear explanation and explicit confirmation
  So that tests do not quietly normalize regressions or unreviewed behavior changes.

## Functional Requirements

- The repository must define a supported end-to-end regression harness for the full Yugastore application.
- The harness must provide a deterministic environment bootstrap path, including service startup prerequisites and seeded data assumptions.
- The first regression suite must cover the current critical application journeys that are practical to automate without inventing missing product behavior.
- The first covered journeys must include application availability, storefront browsing, cart or checkout flow coverage, and the currently supported merchant onboarding or tenant-context flow.
- The regression workflow must produce actionable pass or fail output and preserve enough failure artifacts to diagnose breakage.
- The repository must document how a maintainer runs the regression suite locally.
- The repository must define the expected Copilot workflow for running the regression suite when affected modules or covered journeys change.
- The repository must require that any modification to an existing regression test include a clear explanation of the behavior change or test correction that justifies the update.
- The workflow must require explicit user or issue-backed confirmation before a regression test is relaxed, deleted, or materially repurposed.

## Non-Functional Requirements

- The regression suite should be deterministic against a documented local environment.
- The first supported regression workflow should remain practical for developer use and avoid unnecessary environment complexity.
- Failure output should make it clear whether the breakage comes from startup, data setup, routing, UI behavior, or a downstream service.
- The regression harness should be structured so new end-to-end journeys can be added without rewriting the bootstrap path.

## Constraints

- The application is a multi-service Java 17 and Spring Boot 2.6.3 monorepo with a React 16 frontend wrapper.
- `products-microservice` and `checkout-microservice` use YCQL, while `cart-microservice` and `login-microservice` use YSQL or PostgreSQL-compatible access.
- `login-microservice` is incomplete, so the first regression scope must only automate supported behavior that already exists in the repository.
- Existing local startup and seed workflows under `resources/` and `.github/copilot-instructions.md` should be extended rather than replaced wholesale.
- The final workflow must stay compatible with the repository's PRD to OpenSpec to GitHub issue planning process.

## Assumptions

- The first browser-driven suite will use Playwright as the recommended runner because it can cover UI and API-assisted setup flows against the current architecture without requiring a frontend rewrite.
- The initial regression program will prioritize a small number of high-value smoke and critical-path journeys over exhaustive scenario coverage.
- A documented startup or fixture-reset command can be standardized around the existing YugabyteDB schema and seed assets.
- Copilot instructions can require regression execution when the suite exists and can require explicit justification for test changes even before CI automation is expanded.

## Open Questions

- Should the canonical regression environment use the existing manual startup runbook, `docker-run.sh`, or a dedicated test bootstrap command as the default operator experience?
- Which currently supported merchant onboarding and tenant-context flow should be treated as the baseline regression path if the implementation continues to evolve?
- Should the first suite include the incomplete login service only through supported happy paths, or should some flows remain API-level smoke checks until the product contract stabilizes?
- What maximum runtime is acceptable for the first full local regression pass?

## Risks

- If the bootstrap process is not deterministic, the suite will create noisy failures and lose trust quickly.
- If the first slice tries to automate too many flows at once, the work will stall before maintainers gain useful regression coverage.
- If test changes are allowed without explicit rationale, the suite can drift and stop protecting against regressions.
- If the suite depends on unsupported login behavior, it may encode unstable contracts instead of protecting real ones.

## Dependencies

- Existing startup guidance in `.github/copilot-instructions.md` and checked-in seed assets under `resources/`.
- Service availability across `eureka-server-local`, `api-gateway-microservice`, `products-microservice`, `cart-microservice`, `checkout-microservice`, `login-microservice`, and `react-ui`.
- A chosen end-to-end test runner and project structure for storing the suite.

## Candidate OpenSpec Changes

- `e2e-regression-foundation`: define the regression harness, environment bootstrap, deterministic fixtures, command surface, and failure reporting.
- `e2e-regression-critical-flows`: automate the critical journeys that prove the current application state across UI and cross-service behavior.
- `regression-workflow-guardrails`: update repository workflow guidance and test-governance rules so Copilot runs the suite when relevant and test modifications require explicit justification.

## OpenSpec Change Dependencies

- `e2e-regression-critical-flows` depends on `e2e-regression-foundation`.
- `regression-workflow-guardrails` depends on `e2e-regression-foundation` defining the supported regression command surface.

## Recommended Sequencing

- First, establish the harness, deterministic data reset approach, and documented regression commands.
- Second, automate the current critical journeys that provide useful whole-application regression coverage.
- Third, codify workflow guardrails so Copilot and reviewers enforce regression execution and require explicit rationale for test changes.

## Success Metrics

- Maintainers can run a documented end-to-end regression command and receive a clear pass or fail result.
- The first suite detects breakage in at least the core availability, storefront, and targeted onboarding or purchase journeys.
- Regression test updates include explicit rationale and confirmation instead of silent behavior drift.
- Copilot workflow guidance references the regression suite as a required verification step when covered flows are touched.