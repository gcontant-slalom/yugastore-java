---
name: github-issue-coordination
description: 'Find the next GitHub task to work on, claim issues, reserve path prefixes, and coordinate agent-safe issue workflow in yugastore-java.'
argument-hint: 'Describe whether you want to find the next task, claim an issue, or verify a path lock.'
user-invocable: true
---

# GitHub Issue Coordination

Use this skill when work should be driven by GitHub Issues and you need to avoid multiple agents editing the same area at the same time.

All GitHub issue reads and writes must stay within the current repository context. Do not inspect or borrow conventions from a parent, upstream, or similarly named fork unless the user explicitly requests that.

## When To Use
- Find the next safe issue to work on
- Verify whether an issue is blocked by another active path reservation
- Claim an issue and lock its reserved paths
- Check whether dependencies are resolved before an agent starts coding

For new issues in this repository, the issue should reference its backing OpenSpec change package.

## Workflow
1. Identify candidate issues.
   Favor issues labeled `ready` and avoid issues labeled `blocked` or `agent-locked`.

2. Read the issue body.
   Every issue that can be claimed should declare `OpenSpec Change`, `Reserved Paths`, and `Dependencies`.

3. Validate OpenSpec backing.
   If a new issue does not reference an OpenSpec change package, stop and backfill the OpenSpec change before coding.

4. Compare with active locks.
   Open issues labeled `agent-locked` are treated as active reservations. Compare path prefixes, not just exact file names.

5. Claim only conflict-free work.
   If a candidate issue overlaps with another active reservation or depends on unresolved work, do not claim it.

6. Record the lock.
   Assign the issue, add `agent-locked`, and post the lock comment using the [lock comment template](./assets/lock-comment-template.md).

7. Stop on expansion.
   If the task grows beyond the reserved paths, stop and open a new issue or coordinate with the owner of the overlapping lock.

## Default Path Ownership
Use the repository ownership map in [path ownership](./references/path-ownership.md) as the default split when issues do not already specify a narrower reservation.

## Recommended Commands
- `gh issue list --repo YugabyteDB-Samples/yugastore-java --label ready`
- `gh issue list --repo YugabyteDB-Samples/yugastore-java --label agent-locked --state open`
- `gh issue view <number> --repo YugabyteDB-Samples/yugastore-java --json number,title,body,labels,assignees,url,state`
- `gh issue edit <number> --repo YugabyteDB-Samples/yugastore-java --add-assignee <login> --add-label agent-locked`
- `gh issue comment <number> --repo YugabyteDB-Samples/yugastore-java --body-file <file>`


## Custom Agents
- Use `Next Task Finder` to select the next safe issue without changing GitHub state.
- Use `Issue Locker` to claim one issue and apply the lock only after conflict checks pass.
