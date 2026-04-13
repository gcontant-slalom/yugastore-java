# Copilot Instructions for yugastore-java

## Project Context

- This repository is a Java 17 monorepo for a Spring Boot 2.6.3 microservices ecommerce sample backed by YugabyteDB.
- Main modules: `eureka-server-local`, `api-gateway-microservice`, `products-microservice`, `checkout-microservice`, `cart-microservice`, `login-microservice`, and `react-ui`.
- `products-microservice` and `checkout-microservice` use YCQL. `cart-microservice` and `login-microservice` use YSQL/PostgreSQL-compatible access.
- `react-ui` is a Spring Boot wrapper around the React app in `react-ui/frontend`.
- `login-microservice` is incomplete. Do not invent missing authentication flows unless explicitly asked.

## Development Principles

- Follow the general engineering rules in `docs/development-guidelines.md` in addition to the repo-specific instructions below.
- Prefer the smallest change that solves the requested problem.
- Keep changes scoped to the affected module unless the task clearly requires cross-service updates.
- Preserve existing request/response shapes, ports, service names, and discovery behavior unless the user asks for contract changes.
- Follow the existing Spring Boot style, package layout, naming, and configuration patterns already used in the touched module.
- Do not edit generated output under `target/` or make incidental dependency upgrades.

## Testing Scope

- Use the Maven wrapper (`./mvnw` at the repo root or module root), not a globally installed Maven.
- Prefer targeted verification for the module you changed, for example `./mvnw -pl cart-microservice test`.
- Existing automated tests are mostly basic Spring context-load tests. When changing business logic, add or update focused tests near the affected code when practical.
- For `react-ui`, keep frontend changes inside `react-ui/frontend` and verify the related frontend build path if the UI is modified.
- If a change depends on a running YugabyteDB instance or multiple services, say so clearly instead of claiming full verification.

## Workflow Patterns

- Read the affected module before editing; do not assume patterns from a different service apply here.
- For database work, confirm whether the module uses YCQL or YSQL before changing repositories, queries, or schema assumptions.
- Keep configuration changes local to the owning service and update documentation when startup steps, ports, or data-loading steps change.
- When working with sample data or schema setup, prefer the checked-in assets under `resources/`.
- If UI work requires backend changes, keep the API gateway and the consuming service in sync.

## Git Workflow

- Keep diffs focused and easy to review.
- Ignore unrelated local changes in the worktree and do not revert user work.
- Do not create commits, branches, or rewrite history unless explicitly asked.
- Mention any untested areas, service dependencies, or environment assumptions in the final response.

## Local Environment Notes

- Use Java 17 or newer.
- Prefer `./mvnw`; the repo has known compatibility issues with some global Maven setups.
- For local data loading, prefer the documented `ycqlsh COPY` workflow over legacy `cassandra-loader` flows.