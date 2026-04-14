---
name: Epic
about: Track a multi-issue outcome backed by one or more OpenSpec changes
title: "EPIC: "
labels: ["type:epic"]
assignees: []
---

## Summary

Describe the business or delivery outcome this epic coordinates.

## Scope

Describe the boundaries of the epic and what it does not include.

## OpenSpec Changes

- `openspec/changes/<change-name>/`

## Outcome

- [ ] State the user-facing outcome
- [ ] State the technical outcome

## Acceptance Criteria

- [ ]
- [ ]
- [ ]

## Owning Areas

- [ ] area:auth
- [ ] area:catalog
- [ ] area:cart
- [ ] area:checkout
- [ ] area:gateway
- [ ] area:ui
- [ ] area:shared

## Data Impact

- [ ] db:ysql
- [ ] db:ycql

## Affected Modules

- [ ] login-microservice
- [ ] products-microservice
- [ ] cart-microservice
- [ ] checkout-microservice
- [ ] api-gateway-microservice
- [ ] react-ui
- [ ] resources

## Reserved Paths

List the path prefixes reserved by this epic. Child issues must reserve a subset and must not overlap with another active issue unless there is an explicit dependency.

Example:

- `login-microservice/src/main/java/**`
- `resources/schema.sql`

## Child Issues

- [ ]
- [ ]
- [ ]

## Dependencies

List blocking issues, decisions, or external dependencies.

## Done Definition

- [ ] All child issues are complete
- [ ] Referenced OpenSpec changes are implemented or archived as appropriate
- [ ] Reserved paths were coordinated without overlap
- [ ] Verification notes were added to the final PRs
