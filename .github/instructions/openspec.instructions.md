---
description: "Use when creating or editing OpenSpec artifacts under openspec/. Enforces the repo's lightweight PRD-to-OpenSpec-to-issue workflow."
name: "OpenSpec Guidance"
applyTo: "openspec/**/*.md"
---
# OpenSpec Guidance

- Use OpenSpec as the execution spec layer between PRDs and GitHub issues.
- Treat `resources/prds/*.md` as product intent and `openspec/changes/*` as implementation-ready change contracts.
- Keep change packages bounded. Prefer one OpenSpec change per deployable slice or tightly coupled cross-service change.
- Ask clarifying questions instead of guessing when requirements, dependencies, or ownership are unclear.
- Keep `proposal.md` concise and focused on change scope, capabilities, and impact.
- Keep `design.md` brief unless architectural or migration complexity justifies more detail.
- Use spec deltas only for behavior that changes and make every scenario observable and testable.
- Write `tasks.md` so task groups map cleanly to thin GitHub issues and focused implementation sessions.
- Distinguish facts, assumptions, and unresolved decisions explicitly.
- Avoid duplicating long PRD narrative in OpenSpec artifacts. Reference the PRD path where needed.
