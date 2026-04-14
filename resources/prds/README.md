# PRD Workflow

This directory stores product requirements documents generated for this repository.

## What The Agent Does

The repository includes a custom Copilot agent named `PRD From Docs` that can take raw project documentation such as transcripts, notes, screenshots, whiteboard photos, and diagrams and turn that input into:

- a PRD saved under `resources/prds/`
- Mermaid diagrams saved under `resources/prds/diagrams/` when architectural content is present
- one or more bounded OpenSpec changes under `openspec/changes/`
- thin GitHub issues derived from OpenSpec tasks

## How To Invoke It

Use the custom agent picker and select `PRD From Docs`, then give it a simple prompt such as:

- `Create a PRD from this input`
- `Create a PRD and OpenSpec changes from these notes`
- `Create a PRD, OpenSpec change, and issues from this transcript`
- `Turn this whiteboard photo into a PRD`

The workflow keeps the user prompt simple and puts the complexity inside the agent.

## Where PRDs Are Stored

Generated PRDs should be saved in this directory using a descriptive file name such as:

- `resources/prds/merchant-onboarding.md`
- `resources/prds/storefront-tenancy.md`

The template for new PRDs is [_template.md](./_template.md).

Architectural Mermaid diagrams should be saved under `resources/prds/diagrams/` using names such as:

- `resources/prds/diagrams/merchant-onboarding-system-context.mmd`
- `resources/prds/diagrams/storefront-tenancy-data-flow.mmd`

The diagram template is [resources/prds/diagrams/_template.mmd](./diagrams/_template.mmd).

## How Mermaid Diagrams Are Used

When raw documentation includes architectural descriptions, system interactions, or whiteboard-level component design, the agent should translate that information into Mermaid diagrams.

Those diagrams are intended to help both humans and agents by making:

- component boundaries explicit
- data and request flows visible
- ownership and responsibilities easier to infer

If the architecture is unclear, the workflow should stop and ask clarifying questions instead of guessing at the diagram.

## How OpenSpec Fits

After a PRD is clear enough, the next step is to create one or more bounded OpenSpec changes.

OpenSpec is the execution layer for this repository:

- PRD: product intent, user needs, scope, constraints, dependencies
- OpenSpec change package: execution-ready change contract
- GitHub issue: thin execution wrapper around OpenSpec tasks

OpenSpec changes live under `openspec/changes/<change-name>/` and should include:

- `proposal.md`
- `specs/`
- `design.md` when needed
- `tasks.md`

## How Issue Generation Works

GitHub issues are generated from OpenSpec task groups, not directly from the PRD.

Before generating issue output, the agent is expected to inspect:

- `.github/ISSUE_TEMPLATE/`
- `gh issue list`
- `gh issue view <id>`
- `gh label list`

That inspection ensures the generated issues match the repository's existing structure, title prefixes, labels, dependency notation, OpenSpec reference format, and reserved-path workflow.

The agent should reuse existing labels and should not invent a new issue format when the repository already has one.

## How GitHub Templates Are Respected

The current repository issue templates define three primary issue shapes:

- `EPIC:`
- `STORY:`
- `SPIKE:`

Generated issues should align to those templates and to the live issue examples already present in GitHub Issues.

For new issues, keep the body thin and point back to the backing OpenSpec change package.

## How Clarifying Questions Are Handled

The agent must not guess. When critical information is missing or ambiguous, it should stop and ask for clarification using this format:

```text
NEEDS CLARIFICATION:
- <question>
```

The workflow should only create OpenSpec changes or GitHub issues once the source material is specific enough to support implementation-ready work.

