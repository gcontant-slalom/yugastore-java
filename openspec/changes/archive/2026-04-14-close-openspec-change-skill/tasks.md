# Tasks: Close OpenSpec Change Skill

## Task Group 1: Design & Architecture Review

### 1.1 Review Design Document

- [x] Review the skill workflow, transaction semantics, and rollback logic in design.md
- [x] Confirm decisions on issue discovery strategy (multi-source matching)
- [x] Confirm rollback behavior on GitHub API failure
- [x] Confirm idempotency requirements
- [x] Document any design questions or open decisions

**Acceptance:** Design is reviewed and approved; no blocking questions

---

## Task Group 2: Skill Implementation

### 2.1 Create Skill Definition

- [x] Create `.github/skills/close-openspec-change/SKILL.md`
- [x] Include name, description, and user-invocability metadata
- [x] Document input parameters and validation rules
- [x] Document workflow steps (archive, discovery, filter, transaction, rollback, report)
- [x] Document error handling and edge cases
- [x] Document output format (structured report)

**Acceptance:** Skill definition is complete and follows existing skill templates

### 2.2 Implement Archive Phase

- [x] Validate OpenSpec change exists before any GitHub changes
- [x] Call `openspec archive <change-name>`
- [x] On archive failure: stop and report error without touching GitHub
- [x] Record archive success for downstream phase gating

**Acceptance:** Archive phase tested locally with a sample change

### 2.3 Implement Issue Discovery Phase

- [x] Parse archived change's tasks.md for task group to issue mappings
- [x] Extract task groups and explicit issue references
- [x] Query GitHub API for issues mentioning the change name in body
- [x] Match on reserved paths and dependency sections
- [x] Record all discovered issues

**Acceptance:** Discovery correctly finds related issues; discovered list is logged

### 2.4 Implement Filter Phase

- [x] For each discovered issue, read current labels
- [x] Identify issues with `blocked` or `agent-locked` labels
- [x] Identify issues still blocked by other `blocked-*` labels
- [x] Build unblock queue: issues that have blocking labels and no remaining blockers

**Acceptance:** Filter correctly identifies which issues should be updated

### 2.5 Implement Transaction Phase

- [x] For each unblock-ready issue: snapshot current labels and state
- [x] Remove labels (`blocked`, `agent-locked`) via `gh issue edit`
- [x] Close the issue with an explanatory comment via `gh issue close`
- [x] Track each operation success/failure
- [x] On failure with rollback enabled: trigger rollback phase

**Acceptance:** Label removal and issue closure work; errors are caught

### 2.6 Implement Rollback Phase

- [x] On GitHub API failure (rollback enabled): restore original labels on committed issues
- [x] Re-open closed issues in the committed list
- [x] Attempt to remove closure comments
- [x] Log rollback success/failure per issue (best-effort)

**Acceptance:** Rollback correctly restores prior state; failures are reported

### 2.7 Implement Report Phase

- [x] Output archive status
- [x] Output list of discovered issues and processing result
- [x] Output labels removed summary
- [x] Output issues closed list
- [x] Output rollbacks applied (if any)

**Acceptance:** Report is human-readable and actionable

---

## Task Group 3: Testing & Validation

### 3.1 Unit Tests

- [x] Test issue discovery logic (mock GitHub API responses)
- [x] Test filter logic (various label combinations)
- [x] Test transaction phase (success path)
- [x] Test transaction phase (failure path with rollback)
- [x] Test rollback logic (verify all state is restored)

**Acceptance:** Unit tests pass; coverage > 80%

### 3.2 Integration Testing

- [x] Set up a test OpenSpec change with sample tasks.md
- [x] Create test GitHub issues linked to that change
- [x] Run skill in `--dry-run` mode: verify planned actions without applying
- [x] Run skill in normal mode: verify archive, label removal, issue closure
- [x] Trigger mid-transaction failure and verify rollback

**Acceptance:** Skill works end-to-end on actual GitHub issues; rollback verified

### 3.3 Idempotency Testing

- [x] Run skill on a completed change
- [x] Run skill again on same change
- [x] Verify second run produces same state (no errors, no double-closes)

**Acceptance:** Skill is idempotent

### 3.4 Edge Case Handling

- [x] Handle issue already manually closed before skill runs
- [x] Handle issue with no labels (already clean)
- [x] Handle GitHub API rate limit hit mid-transaction
- [x] Handle invalid or non-existent change name

**Acceptance:** All edge cases handled gracefully

---

## Task Group 4: Documentation & Deployment

### 4.1 Update Skill Documentation

- [x] Add usage examples to SKILL.md
- [x] Document input parameters, triggers, and invocation methods
- [x] Add troubleshooting section for common failures

**Acceptance:** Documentation is complete and clear

### 4.2 Integration Testing with Other Skills

- [x] Test interaction with `issue-locker` agent
- [x] Verify labels and dependency fields used by other tools remain compatible

**Acceptance:** No regressions in issue-coordination workflows

### 4.3 Update Copilot Instructions

- [x] Reference the new skill from `.github/copilot-instructions.md` if appropriate
- [x] Update any relevant agent mode references

**Acceptance:** Skill is discoverable from standard workflow entry points
