---
name: openspec-from-prd
description: 'Create one or more bounded OpenSpec change packages from a completed PRD under resources/prds using the official OpenSpec CLI and current spec-driven schema.'
argument-hint: 'Provide the PRD path under resources/prds and say whether to create one change or split the PRD into multiple bounded changes.'
user-invocable: true
---

# OpenSpec From PRD

Use this skill when a completed PRD exists and the next step is to create OpenSpec change package artifacts for implementation.

## Responsibility
This skill is responsible for PRD to OpenSpec conversion only.

It should:
- read a completed PRD from `resources/prds/`
- decide whether the PRD maps cleanly to one bounded change or should be split into multiple changes
- ask focused clarifying questions when the PRD is too ambiguous for execution planning
- create one or more OpenSpec changes under `openspec/changes/`
- generate `proposal.md`, spec delta files, `design.md`, and `tasks.md` using the official OpenSpec CLI flow
- keep the resulting change packages lean and implementation-ready

It must not:
- create GitHub issues directly from the PRD
- silently choose between conflicting interpretations in the PRD
- collapse multiple independent workstreams into one oversized change package

## Input Requirements
The input must identify a PRD under `resources/prds/`.

If the PRD still has unresolved questions that block execution planning, stop and output exactly:

NEEDS PRD UPDATE:
- <missing detail>

## Workflow
1. Read the PRD and confirm it has enough detail for execution.
   Validate:
   - goals
   - scope
   - functional requirements
   - constraints
   - dependencies
   - open questions
   - candidate change boundaries or equivalent structure

2. Decide the change breakdown.
   - Prefer one OpenSpec change per bounded implementation slice.
   - Split large PRDs into multiple changes when the work can proceed independently or on separate schedules.
   - Keep the dependency graph shallow and explicit.

3. Check for existing OpenSpec changes.
   Use `openspec list --json` to avoid duplicating or conflicting with an active change.

4. Create each change with the official CLI.
   Use:
   - `openspec new change "<name>"`
   - `openspec status --change "<name>" --json`
   - `openspec instructions <artifact-id> --change "<name>" --json`

5. Generate the artifacts in order.
   - `proposal.md` from PRD goals, scope, impact, and capability boundaries
   - `specs/` from behavior changes only
   - `design.md` when technical decisions, migrations, or cross-service details need recording
   - `tasks.md` as bounded implementation slices in dependency order

6. Keep the package lightweight.
   - Reference the PRD path instead of copying long background sections
   - Keep proposal and design concise
   - Use spec deltas for the execution contract
   - Write tasks so each task group can later become one thin GitHub issue or one focused implementation session

7. Validate readiness.
   Use `openspec status --change "<name>" --json` to confirm the change is ready for issue generation or implementation.

## Clarification Rule
When the PRD leaves a material execution question unanswered:
- ask a focused follow-up question
- recommend a default when useful
- distinguish recommendation from confirmed fact
- stop instead of guessing

## Outputs
Return:
- `PRD`: source path
- `OpenSpec changes`: created or updated change names and paths
- `Dependency order`: explicit change ordering or `none`
- `Assumptions`: explicit list or `none`
- `Open questions`: explicit list or `none`
- `Additional decisions needed`: exact follow-up decisions required before issue generation
