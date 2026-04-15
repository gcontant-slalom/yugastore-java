# Close OpenSpec Change and Update Issue Dependencies Skill

## Summary

Create a new AI skill that automates the closure of completed OpenSpec changes and updates all related GitHub issues atomically. The skill will archive the OpenSpec change, identify all related issues, remove blocking labels, and close those issues as part of a single transactional workflow with rollback on failure.

## Scope

**What:** Create the `close-openspec-change` skill that:
- Invokes `openspec archive` to archive a completed change
- Discovers all GitHub issues related to the change (via task IDs, reserved paths, dependencies, affected modules)
- Removes blocking labels (`blocked`, `agent-locked`) from issues that have ALL blockers removed
- Closes only issues directly part of the change's task list
- Implements transaction semantics with rollback on GitHub API failure

**Repository:** gcontant-slalom/yugastore-java only  
**Affected Components:** GitHub issue coordination, OpenSpec workflow automation  

**What's NOT included:**
- Changes to PR merge or review workflows
- Modifications to issue content/body
- Multi-change dependency cascade handling
- Archive undo/revert feature

## Impact

- Eliminates manual multi-step issue cleanup after each OpenSpec change  
- Ensures consistent issue state (no partial updates)
- Reduces coordination errors and improves workflow velocity
- Provides clear audit trail of closure actions

## Success Criteria

- Skill successfully archives an OpenSpec change
- All related issues are discovered and updated
- Blocking labels are removed from unblocked issues
- All issues in the change's task list are closed
- If any step fails, all changes are rolled back consistently
- Skill generates a clear summary report

## Key Constraints

- Must operate only on `gcontant-slalom/yugastore-java` (no parent/upstream forks)
- Transaction semantics: all succeed or all rollback (no partial state)
- No changes to issue body/content (labels and state only)
- Must not close unrelated issues

## Reference

Source PRD: [resources/prds/close-openspec-change-skill.md](../../../../resources/prds/close-openspec-change-skill.md)
