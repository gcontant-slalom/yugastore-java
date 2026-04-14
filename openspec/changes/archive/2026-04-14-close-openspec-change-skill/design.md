# Design: Close OpenSpec Change Skill

## Architecture

The skill is invoked with a change name (e.g., `checkout-second-product-fix`).

### Workflow

```
1. Validation
   - Confirm the OpenSpec change exists
   - Confirm the change's tasks.md lists all related issues
   
2. Archive
   - Run: openspec archive <change-name>
   - If fails: stop and report error (no GitHub state changes yet)
   
3. Issue Discovery (populate from tasks.md + metadata)
   - Read tasks.md task groups
   - Identify issues that correspond to each task group
   - Query GitHub API to find issues with:
     * OpenSpec change name in body
     * Reserved paths matching issue reserved paths
     * Assigned labels matching change's affected module labels
   
4. Filter: Unblocked Issues Only
   - For each discovered issue, check labels
   - Keep only issues that have:
     * An "agent-locked" label OR "blocked" label currently applied
     * No other "blocked-*" labels (indicating all blockers are removed)
   
5. Transaction: Label & Close
   For each unblocked issue:
     - Record current label state (for rollback)
     - Remove labels: "blocked", "agent-locked"
     - Add label: "agent-safe" (optional, if needed)
     - Close the issue with a comment: "Unblocked by OpenSpec change: <change-name>"
   
6. Commit or Rollback
   - If ALL GitHub API calls succeed: commit (success)
   - If ANY call fails:
     - Restore all labels and states to pre-transaction state
     - Report which operations succeeded and which failed
   
7. Report
   - OpenSpec change: archived
   - Issues found: list
   - Issues closed: list
   - Labels removed: summary
   - Rollbacks (if any): detail
```

## Decision: Transaction Behavior

**Issue:** What if one issue update fails mid-stream?

**Decision:** Fail-fast with full rollback.
- When any GitHub API call fails, immediately stop processing new issues
- Revert all label/state changes already applied
- Report exactly which operations succeeded before failure
- This ensures the repository never gets into a partial/inconsistent state

**Rationale:**
- Consistency is more important than partial success in this workflow
- The caller can retry the full operation after resolving the failure
- Clear audit trail of what was attempted and what was rolled back

## Decision: Issue Matching Strategy

**Issue:** How do we find issues related to a change?

**Strategy:** Multi-source matching (all of these):
1. Search GitHub for issues mentioning the OpenSpec change name in body
2. Match on reserved paths: if issue body contains reserved paths that overlap with the change
3. Match on task IDs: if a GitHub issue references a task ID from the change's tasks.md
4. Match on affected module labels: if issue has labels matching the change's affected modules

**Rationale:**
- Provides redundancy and ensures we don't miss related issues
- Falls back gracefully if issue bodies are incomplete  
- Explicit task IDs in issue bodies are the most reliable signal

## Decision: Rollback Mechanism

**Issue:** If rollback fails, what do we do?

**Decision:** Best-effort rollback with error reporting.
- Attempt to restore each label that was changed
- If a rollback call fails, record it but continue rolling back other issues
- Report the final state: which issues rolled back cleanly vs. which have partial state
- Surface instructions for manual cleanup if needed

**Rationale:**
- Some issues may have been manually altered between archive and rollback attempt (rare but possible)
- Best-effort approach avoids cascading failures
- Clear reporting allows manual intervention if needed

## Dependencies

- OpenSpec CLI available in the execution environment
- GitHub CLI (`gh`) with write permissions on gcontant-slalom/yugastore-java
- Ability to read and parse tasks.md from the OpenSpec change directory

## Error Handling

- **OpenSpec archive fails:** Stop before GitHub changes; report error
- **GitHub API rate limit:** Pause and retry (exponential backoff) up to N times
- **GitHub API permission denied:** Stop and report; user must check permissions
- **Issue already closed:** Treat as success (idempotent)
- **Label already removed:** Treat as success (idempotent)
- **Invalid change name:** Stop and report unknown change

## Design Review Notes (Task 1.1)

**Reviewed:** 2026-04-14

### Clarification: Label Signal in Issue Discovery

Signal 4 in the multi-source matching strategy ("match on affected module labels") **must be used only as a narrowing filter**, not as a standalone match. Area labels like `area:checkout` return many unrelated issues across the repository. Implementation must always combine signal 4 with at least one primary signal (change name in issue body, or explicit task ID reference) before treating an issue as related.

### Idempotency Behavior

The skill is implicitly idempotent through the archive phase gate:
- If the OpenSpec change has already been archived, `openspec archive` will fail, and the skill exits before making any GitHub changes.
- Individual GitHub operations (label removal, issue closure) are already treated as idempotent in the Error Handling section ("issue already closed" and "label already removed" are both success cases).
- No explicit second-run protection is required beyond these existing guards.

**All design decisions confirmed. No blocking questions.**
