---
name: prd-from-docs
description: 'Create an implementation-ready PRD from transcripts, documentation, notes, screenshots, images, or other project artifacts.'
argument-hint: 'Provide the raw source material to convert into a PRD. Mixed inputs are supported.'
user-invocable: true
---

# PRD From Docs

Use this skill when a user provides raw project material and wants a structured PRD saved under `resources/prds/`.

## Responsibility
This skill is responsible for PRD generation only.

It should:
- accept mixed-source inputs such as transcripts, notes, docs, screenshots, UI mockups, whiteboard photos, and repository documentation
- extract requirements, goals, scope, assumptions, constraints, risks, and open questions
- identify candidate OpenSpec change boundaries or work areas
- capture dependencies between those change candidates
- ask clarifying questions when information is missing, contradictory, or materially ambiguous
- make explicit recommendations when decisions are needed
- produce an implementation-ready PRD
- save the PRD under `resources/prds/`

It must not:
- create GitHub issues
- create OpenSpec changes before the PRD is clear enough
- guess missing behavior or architecture

## Inputs This Workflow Supports
- Interview or meeting transcripts
- Product or planning notes
- Screenshots and UI mockups
- Whiteboard or architecture photos
- Text documents, markdown files, and repository documentation

## Mermaid Diagram Requirement
When the input includes architectural or system design information, generate Mermaid diagrams and save them under `resources/prds/diagrams/`.

Use Mermaid diagrams to capture:
- system components
- data flows
- service responsibilities
- integration boundaries
- interaction or sequence flows when relevant

Prefer simple, valid Mermaid markup over visually dense diagrams.

## PRD Workflow
1. Gather and inspect the source inputs.
   Read text directly. Use `view_image` for screenshots, diagrams, or whiteboard photos.

2. Extract only meaningful content.
   Keep:
   - goals
   - users or personas
   - scope
   - constraints
   - assumptions
   - decisions
   - dependencies
   - risks
   - open questions
   - functional requirements
   - non-functional requirements

   Remove:
   - filler
   - repetition
   - side conversation that does not change scope or behavior

3. Identify logical work areas.
   Group the work into candidate OpenSpec changes or bounded work areas.
   Capture dependencies between those candidates when visible from the source material.

4. Check for ambiguity.
   If any critical behavior, ownership, scope boundary, or architectural dependency is missing, stop and output:

   NEEDS CLARIFICATION:
   - <question>

5. Generate Mermaid diagrams when architecture is described.
   Save each diagram as `resources/prds/diagrams/<prd-name>-<diagram-name>.mmd`.
   Reference those diagram files from the PRD.

6. Write the PRD.
   Follow [resources/prds/_template.md](../../../resources/prds/_template.md).
   Save the final document under `resources/prds/<prd-name>.md`.

7. Make the PRD implementation-ready.
   Explicitly surface:
   - assumptions
   - open questions
   - risks
   - dependencies
   - candidate OpenSpec changes
   - recommended sequencing
   - additional decisions still needed

## Recommendations Rule
When the source material implies a decision but does not settle it:
- do not guess
- provide a recommendation
- explain the tradeoff briefly
- record it under `Additional decisions needed` or `Open questions`

## Mermaid Diagram Guidance
- Use `flowchart` or `graph` diagrams for system structure and data flow.
- Use `sequenceDiagram` when the source material describes ordered interactions.
- Use concise node labels and consistent naming from the source material.
- Do not guess hidden systems or undocumented responsibilities.
- If a diagram would rely on missing information, ask for clarification first.

## Handoff Rule
If the user also wants execution planning:
- finish the PRD first
- return the saved PRD path
- hand off to the `openspec-from-prd` skill only after the PRD is stable enough for OpenSpec change creation

## Outputs
- A PRD saved in `resources/prds/`
- Mermaid diagrams saved in `resources/prds/diagrams/` when architectural input is present
- A concise summary of assumptions, open questions, risks, dependencies, candidate OpenSpec changes, and missing decisions


