## Context

The repository already uses `.github/copilot-instructions.md` as the main workflow contract for coding sessions. That makes it the right place to encode regression-run expectations and the policy that regression tests cannot be weakened silently.

## Goals / Non-Goals

**Goals:**
- Tell Copilot when relevant regression checks must run.
- Make skipped or blocked regression execution visible in workflow output.
- Prevent silent deletion, relaxation, or repurposing of regression tests.

**Non-Goals:**
- Build CI enforcement in this change.
- Replace normal engineering judgment for non-regression tests outside the protected suite.

## Decisions

### 1. Use `.github/copilot-instructions.md` as the workflow source of truth

Recommended decision: encode the agent-facing regression workflow in the main repository instruction file.

- Why: that file already governs repository-specific coding behavior and verification expectations.
- Alternative considered: keep the policy only in PRD or OpenSpec artifacts.
- Rejected because planning artifacts alone do not shape day-to-day agent behavior.

### 2. Tie regression execution to touched covered areas and stable suite scopes

Recommended decision: require the relevant regression scope for changes that touch covered modules or journeys, using the foundation command surface as the execution target.

- Why: not every change needs the full suite, but covered cross-service changes need more than ad hoc verification.
- Alternative considered: always require the full suite.
- Rejected because it would be unnecessarily expensive for small scoped work once smoke coverage exists.

### 3. Require explicit rationale and confirmation for regression test changes

Recommended decision: treat modifications to existing regression tests as contract changes that need a stated reason and explicit confirmation.

- Why: regression suites lose value quickly if maintainers can weaken them without scrutiny.
- Alternative considered: allow normal test edits without any additional workflow rule.
- Rejected because the user requirement is to prevent silent regression drift.

## Risks / Trade-offs

- [Workflow rule is too vague to follow] -> Refer to named regression scopes and explicit reporting expectations.
- [Developers bypass the policy when blocked] -> Require blocked or skipped runs to be documented explicitly.
- [Guardrails become stale as the suite evolves] -> Tie the rules to the stable command surface defined by the foundation change.

## Migration Plan

- Update Copilot instructions after the harness command surface is defined.
- Align final-response guidance with regression execution and blocker reporting.
- Add the regression-test modification policy once the protected suite paths are known.

## Open Questions

- Whether the policy should name exact protected test path prefixes immediately or phase that in once the regression workspace lands.
