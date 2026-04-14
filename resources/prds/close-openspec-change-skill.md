# AI Skill: Close OpenSpec Change and Update Issue Dependencies

## Problem Statement

When an OpenSpec change is completed and archived, related GitHub issues must be updated to reflect completion status. Currently this is a manual multi-step process: remove blocking labels, remove agent locks, update issue states, and close issues. This skill automates and transactionizes that workflow, ensuring consistency and reducing manual effort and coordination errors.

## Goals

- Automate the cleanup and closure of GitHub issues after an OpenSpec change is archived
- Ensure all blocking labels (`blocked`, `agent-locked`) are removed from related issues
- Close only issues that are directly part of the completed change's task list
- Provide transaction semantics: if any update fails, rollback all changes to a consistent state
- Report a clear summary of actions taken and any rollbacks

## Non-Goals

- Merge or close pull requests
- Change issue/PR review labels or workflow states unrelated to blocking
- Modify OpenSpec change files (archive is performed separately)
- Handle multi-change dependencies (focus on single change closure at a time)

## Background / Context

The current OpenSpec closure workflow requires:
1. Manual OpenSpec archive via OpenSpec CLI
2. Manual identification of all issues related to the change via reserved paths, task IDs, dependencies, and affected modules
3. Manual label editing and issue closure on each related issue
4. Risk of partial state where some issues update and others fail, leaving inconsistent GitHub state

This skill integrates the OpenSpec archive step into a single atomic operation and handles issue dependency cleanup with rollback on failure.

## Personas

- **Agent:** automated GitHub issue coordinator that needs to finalize a completed change
- **Developer/User:** initiates the skill when ready to close a change and expects a clean summary report
- **Repository maintainer:** relies on accurate issue state and dependency tracking

## User Stories

- As an agent, I want to close a completed OpenSpec change and automatically update all related GitHub issues, so the workflow is atomic and consistent
- As an agent, I want transaction semantics (all-or-nothing), so if any issue update fails, the entire operation rolls back and I know what happened
- As a developer, I want a clear report of all actions and rollbacks, so I can verify the closure was correct and debug any failures

## Functional Requirements

- **OpenSpec Archive Integration:** Perform `openspec archive` as part of the skill (not separate)
- **Issue Discovery:** Identify all GitHub issues related to the change via:
  - Issues mentioning the OpenSpec change name in the body
  - Issues with reserved paths that overlap the change's reserved paths
  - Issues listed in the dependency section of the change
  - Issues tagged with affected modules (as labels)
- **Release Only When Unblocked:** Close/unblock only issues that have ALL blockers removed (i.e., no remaining open `blocked-*` labels)
- **Label Cleanup:** Remove labels `blocked` and `agent-locked` from all related issues
- **Issue Closure:** Close issues that are directly part of the change's task list only
- **Transaction Semantics:** If any GitHub API call fails:
  - Revert all labels and state changes applied so far
  - Leave the OpenSpec archive in place (do not undo archive)
  - Report which operations succeeded and which failed
- **Summary Report:** Return clear output including:
  - OpenSpec change archived
  - Issues found and processed
  - Labels removed per issue
  - Issues closed
  - Any rollbacks and reasons

## Non-Functional Requirements

- Skill must be idempotent: running it twice on the same closed change should not fail
- Skill must handle GitHub API rate limits and transient failures gracefully
- Skill must complete within typical workflow timeouts (< 5 minutes)
- Error reporting must be human-readable and actionable

## Constraints

- Must operate only on `gcontant-slalom/yugastore-java` repository
- No changes to issue content or markdown body (only labels and state)
- Must not modify issues in parent/upstream forks
- Must handle edge case where an issue has already been manually closed

## Assumptions

- The OpenSpec change reference in the issue body is the canonical linkage to the change
- Reserved paths in the issue body will exactly match reserved paths in the OpenSpec change
- All issues related to a change are already tagged with the correct initial labels (`blocked`, `agent-locked`)
- The agent running the skill has GitHub API write permissions (issues, labels)
- OpenSpec archive is idempotent and will not corrupt the repository state on partial failures

## Open Questions

- Should the skill attempt to retry failed GitHub API calls, or fail immediately on any error?
- If an issue is manually closed before the skill runs, should it be counted as a success or skipped?
- Should the skill post a comment on each updated issue explaining the rollback reason, or just report to the user/agent?

## Risks

- GitHub API rate limiting may cause partial failures mid-operation
- Concurrent skill invocations on overlapping changes could race and cause inconsistent state
- If rollback fails, the repository state could be left in an inconsistent middle state (some labels removed, others not)
- Overly broad issue matching (e.g., all issues with matching modules) could accidentally close unrelated issues

## Dependencies

- OpenSpec CLI available and configured for the repository
- GitHub CLI (`gh`) with authenticated access to `gcontant-slalom/yugastore-java`
- All related issues must have standardized format (OpenSpec change reference, reserved paths, task IDs in body)
- Skill must be runnable in the git repository context

## Candidate OpenSpec Changes

- `close-openspec-change-skill`: Create the new AI skill that archives a completed OpenSpec change and updates related GitHub issues with atomic transaction semantics

## Success Metrics

- Skill successfully archives the OpenSpec change
- All blocking labels (`blocked`, `agent-locked`) are removed from related issues
- All issues directly tied to the change's task list are closed
- If any step fails, all previous changes are rolled back consistently
- Skill provides a detailed summary report of actions and/or rollbacks
- Skill is idempotent (running it twice produces the same end state)

## Additional Decisions Needed

- Open: Should concurrent skill invocations be serialized (lock per change) or allowed (optimistic concurrency with conflict detection)?
- Open: On GitHub API failure during rollback, should the skill attempt best-effort partial rollback or give up entirely?
- Recommendation: Start conservative with fail-fast on any API error and expand retry logic after initial testing
