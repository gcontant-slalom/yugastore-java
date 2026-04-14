---
name: Feature Story
about: Deliver one shippable slice backed by an OpenSpec change package
title: "STORY: "
labels: ["type:feature"]
assignees: []
---

## Summary

Describe the single execution slice in one short paragraph.

## Scope

List the bounded implementation scope for this issue.

## OpenSpec Change

- Change: `openspec/changes/<change-name>/`

## OpenSpec Tasks

- `X.Y <task group or task reference>`

## Acceptance Criteria

- [ ]
- [ ]
- [ ]

## Affected Modules

- [ ] login-microservice
- [ ] products-microservice
- [ ] cart-microservice
- [ ] checkout-microservice
- [ ] api-gateway-microservice
- [ ] react-ui
- [ ] resources

## Reserved Paths

List the path prefixes this issue owns while in progress. Do not start work if another active issue already reserves overlapping paths unless this issue explicitly depends on it.

Example:

- `api-gateway-microservice/src/main/java/**`
- `react-ui/frontend/src/components/**`

## Dependencies

List blocking issues, decisions, or upstream PRs.

## Verification

List the targeted verification you plan to run.

Example:

- `./mvnw -pl api-gateway-microservice test`

## Done Definition

- [ ] Acceptance criteria met
- [ ] Reserved paths stayed within issue scope
- [ ] OpenSpec tasks referenced by this issue are complete or updated
- [ ] Verification completed or documented as blocked
