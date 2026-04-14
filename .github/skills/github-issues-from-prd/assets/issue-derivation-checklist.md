# Issue Derivation Checklist

Use this checklist when turning a PRD into GitHub issues.

## Epic Decision
- Create one epic when the work spans multiple services, areas, or delivery phases.
- Do not create multiple epics unless there are clearly separate business outcomes.

## Story Sizing
- Each story should be one coherent delivery slice.
- A story should have one primary verification path.
- A story should reserve a bounded set of path prefixes.
- Split stories when they mix unrelated backend, frontend, and schema work without a hard dependency.

## Spike Rules
- Create a spike only when the PRD still has unresolved research or design risk.
- Do not use a spike to hide known implementation work.

## Dependency Rules
- Prefer a shallow dependency graph.
- Dependencies should point from downstream work to upstream enabling work.
- Avoid circular dependencies.

## Label Rules
- Reuse existing labels only.
- Apply `cross-service` only when the issue truly spans multiple services or contracts.
- Apply `db:ysql` or `db:ycql` only when the issue clearly touches those layers.

## Reserved Path Rules
- Reserve path prefixes, not isolated single files, where practical.
- Keep path ownership as narrow as the issue allows.
- Avoid overlapping reserved paths for issues intended to run in parallel.
