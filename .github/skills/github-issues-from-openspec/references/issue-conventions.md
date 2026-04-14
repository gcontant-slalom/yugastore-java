# Issue Conventions

This repository uses OpenSpec-backed issues as thin execution wrappers.

## Title Prefixes
- `EPIC:` for grouped execution across multiple issue slices
- `STORY:` for one bounded implementation slice
- `SPIKE:` for time-boxed research or decision work

## Required Body Sections For New Issues
- `Summary`
- `Scope`
- `Acceptance Criteria`
- `OpenSpec Change`
- `OpenSpec Tasks`
- `Affected Modules`
- `Reserved Paths`
- `Dependencies`
- `Verification`

## Label Guidance
Use only labels that already exist in GitHub.

The current labels support:
- issue type with `type:*`
- work area with `area:*`
- database scope with `db:*`
- coordination state with `blocked`, `ready`, `agent-safe`, and `agent-locked`
- cross-cutting work with `cross-service`

## Live Validation Rule
Before creating issues, always validate conventions again against live GitHub using `gh issue list`, `gh issue view`, and `gh label list`.
