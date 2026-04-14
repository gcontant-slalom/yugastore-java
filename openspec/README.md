# OpenSpec Workflow

This directory contains the OpenSpec execution layer for this repository.

## Role In The Workflow

Use OpenSpec between PRDs and GitHub issues:

1. Raw docs, transcripts, screenshots, or images become a PRD in `resources/prds/`.
2. A clear PRD becomes one or more bounded OpenSpec changes under `openspec/changes/`.
3. GitHub issues are generated from OpenSpec `tasks.md` task groups.
4. Implementation uses the OpenSpec change package as the primary execution context.

## What Belongs Here

- `config.yaml`: repo-specific OpenSpec context and artifact rules
- `changes/`: active and archived change packages
- `specs/`: baseline specs updated when changes are archived

## OpenSpec Change Layout

Each change should stay bounded and lightweight.

Typical files:

- `proposal.md`
- `specs/<capability>/spec.md`
- `design.md` when technical decisions need recording
- `tasks.md`

## Contributor Commands

OpenSpec 1.3.0 requires Node.js 20.19.0 or newer.

Recommended setup:

```bash
npm install -g @fission-ai/openspec@latest
openspec update
```

If your default Node is older, run the CLI with a Node 20 runtime first.

Common commands:

```bash
openspec list
openspec new change <change-name>
openspec status --change <change-name>
openspec show <change-name>
openspec validate <change-name>
openspec update
```

Common GitHub Copilot prompts after initialization:

```text
/opsx:propose <change-name-or-prd-path>
/opsx:apply <change-name>
/opsx:archive <change-name>
/opsx:explore <topic-or-change-name>
```

## Working Rules

- Keep one change package per bounded implementation slice.
- Keep issue bodies thin and reference OpenSpec artifacts instead of repeating them.
- Ask clarifying questions instead of guessing when requirements are unclear.
- Treat the OpenSpec change package as the source of truth for implementation.
