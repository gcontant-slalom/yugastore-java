# Specification: Close OpenSpec Change Skill Behavior

## Input

```
skill invoke close-openspec-change \
  --change <change-name> \
  [--repo gcontant-slalom/yugastore-java] \
  [--dry-run] \
  [--rollback-on-error true|false]
```

**Parameters:**
- `--change`: Required. Name of the OpenSpec change to close (e.g., `checkout-second-product-fix`)
- `--repo`: Optional. GitHub repository. Defaults to current repo; must be `gcontant-slalom/yugastore-java`
- `--dry-run`: Optional. Preview changes without applying them (default: false)
- `--rollback-on-error`: Optional. Rollback all changes if any step fails (default: true)

## Behavior: Archive Phase

1. Validate the change exists: `openspec status --change <change-name>`
2. If not found: exit with error "Change not found: <change-name>"
3. If found: run `openspec archive <change-name>`
   - If archive fails: exit with error and do NOT proceed to GitHub updates
   - If archive succeeds: continue to issue discovery

## Behavior: Issue Discovery Phase

1. Read the archived change's `tasks.md`
2. Parse task groups and task descriptions
3. Extract issue indicators (issue numbers, task IDs, reserved paths)
4. Query GitHub API:
   ```
   gh issue list --repo gcontant-slalom/yugastore-java \
     --search '<change-name> in:body' \
     --json number,title,body,labels,state
   ```
5. For each returned issue:
   - Check if it mentions the OpenSpec change name
   - Check if it overlaps reserved paths
   - Record as "related issue"

## Behavior: Filter Phase

For each related issue:
1. Check current labels: does it have `blocked` or `agent-locked`?
2. Check remaining blockers: does it have any other `blocked-*` labels?
3. If has blocking labels AND no other blockers → add to "unblock queue"
4. If already unblocked (no `blocked`/`agent-locked` labels) → skip (already clean)

## Behavior: Transaction Phase (Label + Close)

For each issue in the unblock queue:
1. **Pre-transaction snapshot:** Record current labels and state
2. **Remove labels:**
   ```
   gh issue edit <issue-number> \
     --remove-label "blocked" \
     --remove-label "agent-locked" \
     --repo gcontant-slalom/yugastore-java
   ```
3. **Close issue:**
   ```
   gh issue close <issue-number> \
     --comment "Unblocked by OpenSpec change: <change-name>" \
     --repo gcontant-slalom/yugastore-java
   ```
4. **On success:** Add issue to "committed list"
5. **On failure:**
   - If `--rollback-on-error true`: trigger rollback phase
   - If `--rollback-on-error false`: log error and continue

## Behavior: Rollback Phase

If any GitHub operation fails and `--rollback-on-error true`:

1. For each committed issue (successfully updated):
   - Restore original labels
   - Re-open the issue
   - Remove the "Unblocked by..." comment
2. Report final state:
   - Issues successfully rolled back
   - Any rollback failures (e.g., label re-add failed)

## Behavior: Report Phase

Output a structured summary:

```
=== Close OpenSpec Change: <change-name> ===

Archive Status: SUCCESS | FAILED
  - OpenSpec change archived at: openspec/changes/<change-name>-archived/

Issues Found: <count>
  - #123: Product checkout fix [blocked -> cleaned]
  - #456: Inventory validation [blocked -> cleaned]
  - #789: Price calculation [already clean, skipped]

Labels Removed: <total>
  - blocked: 2 removed
  - agent-locked: 2 removed

Issues Closed: <count>
  - #123, #456

Rollbacks: NONE | <count> applied
  - Issue #123: successfully restored
  - Issue #456: label restoration failed (manual fix needed)

Summary: ✓ All operations succeeded | ✗ Partial failure with rollback | ✗ Unrecoverable error
```

## Acceptance Criteria

1. Skill archives the OpenSpec change successfully
2. All related issues are discovered
3. All unblocked issues have `blocked` and `agent-locked` labels removed
4. All issues directly in the change's task list are closed
5. On GitHub API failure, all prior changes are rolled back
6. Skill outputs a clear summary report
7. Skill is idempotent: running twice produces same end state
