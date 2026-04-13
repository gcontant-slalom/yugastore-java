---
name: Feature Story
about: Deliver one shippable slice within a bounded module or path set
title: "STORY: "
labels: ["type:feature"]
assignees: []
---

## Goal

Describe the single delivery slice.

## User Story

As a
I want
So that

## Acceptance Criteria

- [ ]
- [ ]
- [ ]

## Owner

Primary owner for this issue.

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

## Draft PR Rule

Once coding starts, open a draft PR and repeat the reserved paths there.

## Done Definition

- [ ] Acceptance criteria met
- [ ] Reserved paths stayed within issue scope
- [ ] Verification completed or documented as blocked