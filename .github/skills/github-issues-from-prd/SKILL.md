---
name: github-issues-from-prd
description: 'Create GitHub issues from a completed PRD in resources/prds using this repository''s issue templates, labels, and live GitHub conventions.'
argument-hint: 'Provide the PRD path under resources/prds and say whether you want draft-only output or live GitHub issue creation.'
user-invocable: true
---

# GitHub Issues From PRD

Use this skill when a completed PRD already exists and the user wants logical GitHub issues created from it.

## Responsibility
This skill is responsible for GitHub issue creation only.

It should:
- read a completed PRD from `resources/prds/`
- convert the PRD into a sensible issue graph
- preserve dependencies, sequencing, and implementation flow
- separate foundational work from feature work
- keep issues clear, scoped, and actionable
- use the existing repository issue templates
- inspect GitHub live before creating issues
- create issues that are implementation-friendly for Copilot-driven work

It must not:
- create or rewrite the PRD from raw material
- guess missing requirements that are absent from the PRD
- create bloated or vague issues
- duplicate long shared context when the PRD already captures it

## Input Requirements
The input must identify a PRD under `resources/prds/`.

If the PRD is incomplete, ambiguous, or missing dependency information needed for issue creation, stop and output exactly:

NEEDS PRD UPDATE:
- <missing detail>

## Required Repository Checks
Before generating any issue output, always do all of the following:

1. Inspect issue templates in `.github/ISSUE_TEMPLATE/`.
2. Run `gh issue list --repo gcontant-slalom/yugastore-java`.
3. Inspect representative issues with `gh issue view <id> --repo gcontant-slalom/yugastore-java --json number,title,body,labels,url,state`.
4. Review current labels with `gh label list --repo gcontant-slalom/yugastore-java`.

Use the issue template files, [issue conventions](./references/issue-conventions.md), and live GitHub as the source of truth.

## Issue Generation Workflow
1. Read the PRD from `resources/prds/`.

2. Validate issue readiness.
   Confirm the PRD clearly defines:
   - goals
   - scope
   - functional requirements
   - constraints
   - dependencies
   - open questions
   - enough detail to produce actionable implementation slices

3. Derive the issue graph.
   Break the work into issue groupings using the [issue derivation checklist](./assets/issue-derivation-checklist.md).
   - create one epic when the work spans multiple services, areas, or delivery phases
   - create stories for coherent delivery slices
   - create spikes only for unresolved research or design risk
   - keep the dependency graph shallow and explicit

4. Separate work types.
   - foundation or setup work first
   - schema or contract changes before dependent feature work
   - backend and frontend issues separated unless a single slice truly must span both
   - cross-service labels only when the issue really crosses service boundaries

5. Keep issues implementation-friendly.
   Each issue should include:
   - a clear goal
   - bounded scope
   - explicit acceptance criteria
   - affected modules
   - reserved paths
   - dependencies
   - targeted verification

6. Avoid duplicating PRD context.
   Reference the PRD path in the issue body where useful.
   Keep repeated background short.
   Put only issue-local context in each issue.

7. Create output.
   - create live issues with `gh issue create` unless the user asked for a draft-only run
   - preserve logical sequencing and dependencies in the created issue set

## Template Matching Rules
Match the current repository structure:
- epics use `EPIC:` titles
- stories use `STORY:` titles
- spikes use `SPIKE:` titles

Match the existing issue sections from the current templates:
- `Goal`
- `Outcome` or `User Story`
- `Acceptance Criteria`
- `Affected Modules`
- `Reserved Paths`
- `Dependencies`
- `Verification` where applicable
- `Done Definition`

## Label Rules
Use only labels that already exist in GitHub.

Prefer the current taxonomy when supported by the PRD:
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

## Sizing Rules
- issues must be independently actionable
- avoid oversized issues that mix unrelated work
- avoid tiny issues with no standalone verification path
- each implementation issue should reserve a bounded path set
- dependencies must be explicit and non-circular

## Outputs
Return:
- `PRD`: source path
- `Issues`: created issue numbers and URLs, or `draft only`
- `Dependency order`: explicit issue sequencing
- `Labels used`: explicit list
- `Assumptions`: explicit list or `none`
- `Open questions`: explicit list or `none`
- `Risks`: explicit list or `none`
- `Additional decisions needed`: exact follow-up decisions required before implementation
