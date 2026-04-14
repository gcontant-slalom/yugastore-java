---
name: close-openspec-change
description: Archive a completed OpenSpec change and atomically update all related GitHub issues — removing blocking labels and closing task issues. Use when the user wants to finalize and archive a change after implementation is complete.
license: MIT
compatibility: Requires openspec CLI and GitHub CLI (gh).
metadata:
  author: openspec
  version: "1.0"
---

Close a completed OpenSpec change and clean up all related GitHub issues with transactional semantics.

**Responsibility:** This skill archives one OpenSpec change and updates GitHub issue state for issues directly tied to that change. It does NOT modify issue body content, cascade dependency closures across multiple changes, or interact with PRs.

**Input**: A change name (e.g., `checkout-second-product-fix`). If omitted, infer from context or prompt.

---

## Steps

### 1. Validate Input

- Confirm the repository is `gcontant-slalom/yugastore-java`:
  ```bash
  gh repo view --json nameWithOwner --jq '.nameWithOwner'
  ```
  If it returns anything other than `gcontant-slalom/yugastore-java`, stop and report the mismatch.

- Confirm the OpenSpec change exists:
  ```bash
  openspec status --change "<change-name>" --json
  ```
  If not found: exit with error "Change not found: <change-name>". Make no GitHub changes.

---

### 2. Archive the Change

```bash
openspec archive <change-name>
```

- If archive **fails**: stop and report the error. Do not proceed to GitHub updates.
- If archive **succeeds**: note the archive path for the report and continue.

> This is the point of no return for the OpenSpec side. GitHub changes only happen after this succeeds.

---

### 3. Discover Related Issues

Read the archived `tasks.md` to extract task groups and any inline issue references.

Then query GitHub for related issues:

```bash
gh issue list --repo gcontant-slalom/yugastore-java \
  --state open \
  --search '"<change-name>" in:body' \
  --json number,title,body,labels,state
```

For each returned issue, check whether it is related using these signals (in priority order):

| Signal | Weight |
|---|---|
| Issue body contains the OpenSpec change name | Primary |
| Issue body references a task ID from the change's tasks.md | Primary |
| Issue body lists reserved paths overlapping with the change | Secondary |
| Issue has area labels matching the change's affected modules | Narrowing filter only — never sufficient alone |

Record all issues matching at least one **primary** signal as "discovered issues".

---

### 4. Filter: Build the Unblock Queue

For each discovered issue:

1. Read current labels.
2. If the issue has `blocked` or `agent-locked` **and** has no other `blocked-*` labels that still apply → add to **unblock queue**.
3. If the issue already has neither `blocked` nor `agent-locked` → skip (already clean).

---

### 5. Transaction: Update Labels and Close

For each issue in the unblock queue:

**Pre-transaction snapshot:** record current labels and open/closed state.

```bash
# Remove blocking labels
gh issue edit <number> \
  --remove-label "blocked" \
  --remove-label "agent-locked" \
  --repo gcontant-slalom/yugastore-java

# Close with explanatory comment
gh issue close <number> \
  --comment "Closed: associated OpenSpec change '<change-name>' archived. All task criteria met." \
  --repo gcontant-slalom/yugastore-java
```

- On **success**: add to committed list, continue to next issue.
- On **failure** (and `--rollback-on-error true`): immediately stop processing new issues and trigger the Rollback phase.
- On **failure** (and `--rollback-on-error false`): log error and continue.

---

### 6. Rollback Phase (if triggered)

For each issue in the committed list (successfully updated so far):

```bash
# Restore original labels
gh issue edit <number> \
  --add-label "<original-label-1>,<original-label-2>" \
  --repo gcontant-slalom/yugastore-java

# Re-open the issue
gh issue reopen <number> \
  --repo gcontant-slalom/yugastore-java
```

- Attempt rollback for each committed issue independently (best-effort).
- If a rollback call fails, record it but continue rolling back the others.
- After attempting all rollbacks, report which succeeded and which require manual intervention.

---

### 7. Report

Output a structured summary:

```
=== Close OpenSpec Change: <change-name> ===

Archive Status: SUCCESS | FAILED
  Path: openspec/changes/archive/YYYY-MM-DD-<change-name>/

Issues Discovered: <count>
  - #<n>: <title> [updated | skipped (already clean) | failed]

Labels Removed:
  - blocked: <n> removed
  - agent-locked: <n> removed

Issues Closed: <count>
  - #<n>, #<n>, ...

Rollbacks: NONE | <count> applied
  - #<n>: restored | label restoration failed (manual fix needed)

Summary: ✓ All operations succeeded
       | ✗ Partial failure — <n> issue(s) rolled back
       | ✗ Unrecoverable error — manual cleanup required
```

---

## Parameters

| Parameter | Required | Default | Description |
|---|---|---|---|
| `--change` | Yes | — | Name of the OpenSpec change to close |
| `--repo` | No | current repo | Must resolve to `gcontant-slalom/yugastore-java` |
| `--dry-run` | No | `false` | Preview all planned actions without applying any |
| `--rollback-on-error` | No | `true` | Roll back all changes if any GitHub call fails |

### Dry-Run Mode

When `--dry-run` is set, print all planned actions but take no real action:

```
[DRY RUN] Would archive: openspec/changes/<change-name>
[DRY RUN] Would remove labels from #<n>: blocked, agent-locked
[DRY RUN] Would close #<n> with comment
```

---

## Error Handling

| Scenario | Behavior |
|---|---|
| Change not found | Exit before any changes; report error |
| `openspec archive` fails | Exit before GitHub changes; report error |
| GitHub API call fails (rollback on) | Stop, rollback all committed changes, report |
| GitHub API call fails (rollback off) | Log error, continue remaining issues |
| Issue already closed | Treat as success (idempotent) |
| Label already removed | Treat as success (idempotent) |
| GitHub API rate limit | Pause, retry with exponential backoff (max 3 retries); if still failing, trigger rollback |
| Invalid repo (not gcontant-slalom/yugastore-java) | Exit immediately; report mismatch |
| Rollback fails for one issue | Record failure, continue rolling back others; surface manual-cleanup instructions |

---

## Idempotency

Running the skill twice on the same change is safe:
- `openspec archive` will fail on the second run because the change is already archived, causing the skill to exit before any GitHub changes.
- Individual GitHub operations (label removal, issue closure) are treated as success when the target state already exists.

---

## Usage Examples

```
# Normal run
close-openspec-change --change checkout-second-product-fix

# Preview without applying
close-openspec-change --change checkout-second-product-fix --dry-run

# Run without rollback on error
close-openspec-change --change checkout-second-product-fix --rollback-on-error false
```

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| "Change not found" | Change name typo or already archived | Check `openspec list` and `openspec/changes/archive/` |
| Archive fails | Incomplete artifacts or tasks | Verify all tasks are checked off in `tasks.md` |
| Labels not removed | Issue already had labels stripped | Check skill output — "skipped (already clean)" is expected |
| Rollback failed for #N | GitHub API error or permission issue | Manually restore labels and re-open via `gh issue edit` and `gh issue reopen` |
| Partial state after interrupted run | Network/API failure mid-transaction | Re-run; idempotency ensures safe retry after rollback |
