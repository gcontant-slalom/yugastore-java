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

## GitHub Issue Coordination

- For issue-driven work, only select tasks from GitHub Issues instead of starting ad hoc code changes.
- When inspecting issues, labels, templates, or PRs for workflow decisions, use only the current repository context, not a parent, upstream, or similarly named fork. In this workspace that repository is `gcontant-slalom/yugastore-java`.
- For GitHub issue, label, and PR commands, prefer running from the repository root without `--repo`; if a command must pass `--repo`, it must use `gcontant-slalom/yugastore-java`.
- For new implementation work, use the OpenSpec change package referenced by the issue as the primary execution context.
- If an issue lacks an OpenSpec change reference, stop and create or update the backing OpenSpec change before coding.
- Before editing code for a GitHub issue, confirm the issue declares reserved paths and does not conflict with another open issue labeled `agent-locked`.
- Do not work inside path prefixes already reserved by another open `agent-locked` issue unless the user explicitly overrides the workflow.
- Prefer the custom `Next Task Finder` agent to select the next safe issue and the custom `Issue Locker` agent to claim and lock an issue before implementation starts.
- If the planned change expands beyond the reserved paths, stop and ask for a new issue or a coordination decision instead of continuing.

## PRD Generation Workflow

- When the user asks to create a PRD from transcripts, screenshots, whiteboard photos, or raw documentation, prefer the custom `PRD From Docs` agent.
- Save generated PRDs under `resources/prds/`.
- When architectural documentation is present, convert it into Mermaid diagrams under `resources/prds/diagrams/` and reference those files from the PRD.
- After a PRD is clear enough, create one or more bounded OpenSpec changes under `openspec/changes/` before generating GitHub issues.
- Generate GitHub issues from OpenSpec tasks, not directly from the PRD.
- Before generating any GitHub issue output from an OpenSpec change, inspect `.github/ISSUE_TEMPLATE/`, run `gh issue list`, and inspect representative issues with `gh issue view` against the current repository only so the issue structure matches this repository's conventions.
- Reuse only existing GitHub labels unless the user explicitly asks to change the taxonomy.
- If the source material is ambiguous, stop and ask clarifying questions rather than guessing at requirements or issue scope.

## OpenSpec Workflow

- Use the official OpenSpec CLI workflow in this repository.
- The active schema is `spec-driven` under `openspec/config.yaml`.
- Use `openspec init --tools github-copilot` and `openspec update` when refreshing generated guidance.
- Keep OpenSpec artifacts lightweight: `proposal.md` for why and scope, `specs/` for behavior changes, `design.md` only when decisions need recording, and `tasks.md` for implementation slices.
- Treat GitHub issues as thin execution wrappers that reference the OpenSpec change and task groups.

## Local Environment Notes

- Use Java 17 or newer.
- Prefer `./mvnw`; the repo has known compatibility issues with some global Maven setups.
- For local data loading, prefer the documented `ycqlsh COPY` workflow over legacy `cassandra-loader` flows.
