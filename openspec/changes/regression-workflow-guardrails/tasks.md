## 1. Copilot Workflow Updates

- [ ] 1.1 Update `.github/copilot-instructions.md` to require the relevant supported regression scope for covered changes.
- [ ] 1.2 Define how agent output must report passed, blocked, or skipped regression verification.

## 2. Regression Test Change Governance

- [ ] 2.1 Add repository guidance that regression tests cannot be relaxed, removed, or repurposed without explicit rationale.
- [ ] 2.2 Add repository guidance that such regression test changes require explicit user or issue-backed confirmation.

## 3. Workflow Validation

- [ ] 3.1 Validate that the workflow guidance points to the supported regression command surface from `e2e-regression-foundation`.
- [ ] 3.2 Document fallback expectations when the relevant regression scope is unavailable.