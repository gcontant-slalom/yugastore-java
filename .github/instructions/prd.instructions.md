---
description: "Use when creating or editing PRDs under resources/prds. Enforces the repo PRD structure, implementation-ready sections, and issue-aware planning expectations."
name: "PRD Guidance"
applyTo: "resources/prds/**/*.md"
---
# PRD Guidance

- Keep PRDs implementation-ready and decision-oriented.
- Use the standard sections defined by the repository PRD template.
- When architecture or system design information is available, reference Mermaid diagrams stored under `resources/prds/diagrams/`.
- Always include explicit `Assumptions`, `Open questions`, `Risks`, `Dependencies`, and `Success metrics` sections.
- Do not bury unresolved ambiguity inside narrative paragraphs. List it under `Open questions` or stop and ask for clarification.
- If the PRD implies GitHub issues, ensure the PRD is specific enough to derive issue scope, dependencies, and reserved paths.
- Keep PRDs focused on the product and implementation requirements. Do not embed full GitHub issue bodies inside the PRD.
- Prefer concise, concrete bullets over long prose when listing requirements and constraints.
