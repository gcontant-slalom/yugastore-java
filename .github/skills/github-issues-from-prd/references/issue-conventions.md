# Issue Conventions

This repository already has issue templates and live sample issues. Treat them as the baseline format for PRD-derived issues.

## Current Title Prefixes
- `EPIC:` for cross-cutting work
- `STORY:` for delivery slices
- `SPIKE:` for research tasks

## Current Body Structure

### Epic issues
- `Goal`
- `Outcome`
- `Acceptance Criteria`
- `Owning Areas`
- `Data Impact`
- `Affected Modules`
- `Reserved Paths`
- `Child Issues`
- `Dependencies`
- `Done Definition`

### Feature stories
- `Goal`
- `User Story`
- `Acceptance Criteria`
- `Owner`
- `Affected Modules`
- `Reserved Paths`
- `Dependencies`
- `Verification`
- `Draft PR Rule`
- `Done Definition`

### Spikes
- `Question`
- `Why This Matters`
- `Scope`
- `Reserved Paths`
- `Deliverable`
- `Time Box`
- `Done Definition`

## Label Guidance
Use only labels that already exist in GitHub.

The current labels support:
- issue type with `type:*`
- work area with `area:*`
- database scope with `db:*`
- coordination state with `blocked`, `ready`, `agent-safe`, and `agent-locked`
- cross-cutting work with `cross-service`

## Dependency Style
- Dependencies are written explicitly in a `Dependencies` section as bullets.
- Parent epic relationships are represented directly in the issue body.
- Avoid circular references.

## Reserved Paths
- Implementation issues should declare the exact path prefixes they expect to touch.
- Do not reuse a path prefix already owned by an open `agent-locked` issue.

## Live Validation Rule
Before creating issues, always validate these conventions again against live GitHub using `gh issue list`, `gh issue view`, and `gh label list`.
