## 1. Copilot Workflow Updates

- [x] 1.1 Update `.github/copilot-instructions.md` to require the relevant supported regression scope for covered changes.
- [x] 1.2 Define how agent output must report passed, blocked, or skipped regression verification.

## 2. Regression Test Change Governance

- [x] 2.1 Add repository guidance that regression tests cannot be relaxed, removed, or repurposed without explicit rationale.
- [x] 2.2 Add repository guidance that such regression test changes require explicit user or issue-backed confirmation.

## 3. Workflow Validation

- [x] 3.1 Validate that the workflow guidance points to the supported regression command surface from `e2e-regression-foundation`.
- [x] 3.2 Document fallback expectations when the relevant regression scope is unavailable.

Validation note: workflow guidance now references `docs/e2e-command-surface.md`, `docs/e2e-scope-matrix.md`, and `docs/e2e-governance.md`. The command-surface doc points back to `docs/e2e-testing.md` for the supported harness behavior and records fallback expectations when wrapper commands are blocked. The local OpenSpec CLI did not resolve repo changes from this shell, so validation was completed against the checked-in change package and updated repository files directly.