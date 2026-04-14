# Issue Derivation Checklist

Use this checklist when turning an OpenSpec change package into GitHub issues.

## Source Of Truth
- Derive issue slices from `tasks.md` first.
- Derive acceptance criteria from spec delta files.
- Use proposal and design only for concise context and dependency hints.

## Sizing Rules
- Prefer one issue per bounded task group.
- Split a task group if it spans unrelated modules, multiple verification paths, or parallelizable work.
- Do not create one issue per checkbox unless the task list is already naturally issue-sized.

## Dependency Rules
- Preserve task-group sequencing from `tasks.md`.
- Use explicit `Dependencies` bullets in issue bodies.
- Avoid circular dependencies.

## Issue Body Rules
- Keep the body thin.
- Reference `openspec/changes/<name>/` artifacts instead of copying long prose.
- Include OpenSpec change and task references in every new issue.

## Reserved Path Rules
- Reserve path prefixes, not isolated files, where practical.
- Keep path ownership as narrow as the issue allows.
- Avoid overlapping reserved paths for issues intended to run in parallel.
