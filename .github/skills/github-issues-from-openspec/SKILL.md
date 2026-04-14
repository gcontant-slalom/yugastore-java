---
name: github-issues-from-openspec
description: 'Create thin GitHub issues from an OpenSpec change package using tasks.md, spec deltas, existing issue templates, and live GitHub conventions.'
argument-hint: 'Provide an OpenSpec change name or path under openspec/changes and say whether you want draft-only output or live GitHub issue creation.'
user-invocable: true
---

# GitHub Issues From OpenSpec

Use this skill when an OpenSpec change already exists and the next step is to create or update GitHub issues for execution.

## Responsibility
This skill is responsible for OpenSpec to GitHub issue conversion only.

It should:
- read `proposal.md`, `specs/`, `design.md`, and `tasks.md` from an OpenSpec change package
- derive thin, executable issues from `tasks.md` task groups and supporting OpenSpec context
- preserve dependencies and sequencing from the change package
- keep issue bodies short and point back to OpenSpec artifacts
- use the existing issue templates and live GitHub conventions in this repository

It must not:
- create issues directly from a PRD
- duplicate long shared context already captured in OpenSpec
- create vague or oversized issues that mix unrelated work

## Input Requirements
The input must identify an OpenSpec change under `openspec/changes/`.

If the change is missing required execution artifacts, stop and output exactly:

NEEDS OPENSPEC UPDATE:
- <missing detail>

## Required Repository Checks
Before generating any issue output, always do all of the following:

1. Inspect issue templates in `.github/ISSUE_TEMPLATE/`.
2. Run `gh issue list --repo gcontant-slalom/yugastore-java`.
3. Inspect representative issues with `gh issue view <id> --repo gcontant-slalom/yugastore-java --json number,title,body,labels,url,state`.
4. Review current labels with `gh label list --repo gcontant-slalom/yugastore-java`.

Use the issue template files, [issue conventions](./references/issue-conventions.md), and live GitHub as the source of truth.

## Workflow
1. Read the change package.
   Use the change artifacts as the source of truth:
   - `proposal.md`
   - `specs/**/*.md`
   - `design.md` when present
   - `tasks.md`

2. Validate issue readiness.
   Confirm the change package provides:
   - clear scope
   - explicit behavior changes
   - implementation sequencing
   - enough task detail to derive bounded issues

3. Break the work into issue slices.
   Use the [issue derivation checklist](./assets/issue-derivation-checklist.md).
   - Prefer one issue per task group unless a task group is too large.
   - Create an epic only when the change spans multiple issue slices that benefit from grouping.
   - Create a spike only for unresolved research work explicitly present in the change package.

4. Keep issues thin.
   Each issue should include:
   - `Summary`
   - `Scope`
   - `Acceptance Criteria`
   - `OpenSpec Change`
   - `OpenSpec Tasks`
   - `Dependencies`
   - `Affected Modules`
   - `Reserved Paths`
   - `Verification`

5. Derive content from OpenSpec.
   - Use `tasks.md` to determine issue slices and dependency order.
   - Use specs to derive acceptance criteria.
   - Use proposal and design only for concise context, not for long narrative issue bodies.

6. Create output.
   - Create live issues with `gh issue create` unless the user asked for a draft-only run.
   - Keep titles concise and actionable.
   - Include file-path references to the OpenSpec change package rather than copying large blocks of text.

## Label Rules
Use only labels that already exist in GitHub.

Prefer the current taxonomy when supported by the change package:
- `type:epic`
- `type:feature`
- `type:spike`
- `area:*`
- `db:ysql`
- `db:ycql`
- `cross-service`
- `blocked`
- `ready`
- `agent-safe`

## Outputs
Return:
- `OpenSpec change`: source path
- `Issues`: created issue numbers and URLs, or `draft only`
- `Dependency order`: explicit issue sequencing
- `Labels used`: explicit list
- `Assumptions`: explicit list or `none`
- `Open questions`: explicit list or `none`
- `Additional decisions needed`: exact follow-up decisions required before implementation
