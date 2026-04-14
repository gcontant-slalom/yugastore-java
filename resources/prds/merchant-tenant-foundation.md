# Merchant Tenant Foundation

## Problem Statement

The requirements transcript describes a second persona beyond the shopper: a company that needs to sign up, receive its own tenant, and manage a tenant-specific storefront. Yugastore does not currently provide a merchant onboarding flow, a tenant identity, or a browser-visible way to select and demonstrate different tenants.

## Goals

- Allow a prospective merchant company to start onboarding from a shared signup link.
- Create a merchant company or store record that is distinct from shopper identity.
- Assign a unique tenant context during onboarding.
- Provide a local-demo-friendly way to resolve tenant context in the browser.
- Separate merchant-facing onboarding behavior from shopper-facing browsing behavior.

## Non-Goals

- Payment gateway integration.
- Employee role management beyond the initial merchant admin context.
- Domain-based tenant routing or custom DNS setup.
- Full merchant catalog administration interfaces.
- Merchant dashboards, reporting, or fraud tooling.

## Background / Context

This PRD is derived from [resources/requirements.pdf](../requirements.pdf). The transcript explicitly distinguishes two personas:

- shoppers, who have shopping history, order status, and normal buying behavior
- companies, which need onboarding, tenant assignment, and a merchant-oriented portal

The transcript also states that a salesperson should be able to send a link to a prospective merchant, and that the demo can use path-based routing rather than domain-based routing. It further suggests that a shopper should be able to use the same account across multiple tenants if that is the simpler first implementation.

## Architecture / Diagram References

- [resources/prds/diagrams/merchant-platform-context.mmd](diagrams/merchant-platform-context.mmd)
- [resources/prds/diagrams/merchant-onboarding-sequence.mmd](diagrams/merchant-onboarding-sequence.mmd)

## Personas

- Merchant company representative who needs to onboard a store.
- Sales or internal team member who shares the company signup entry point.
- Shopper who needs to browse a tenant-specific storefront.

## User Stories

- As a merchant representative
  I want to open a company signup link and register my store
  So that I can start selling through the platform.

- As a shopper
  I want the browser route to identify which store I am browsing
  So that I see the correct tenant-specific catalog.

- As a delivery team member
  I want tenant selection to be easy to demonstrate locally
  So that the feature can be shown without custom domain setup.

## Functional Requirements

- The system must expose a merchant company signup entry point that can be shared as a link.
- Merchant signup must not require the merchant company to pre-exist as a shopper account.
- Merchant signup must capture the minimum company or store identity needed to create a tenant context.
- The system must create a unique tenant context for each onboarded merchant company or store.
- Merchant company or store identity must be modeled separately from shopper identity.
- The system must provide a merchant-facing onboarding route that is distinct from the shopper storefront route.
- The system must resolve tenant context from a local-demo-friendly browser path.
- Requests that include a supported tenant route must resolve the corresponding tenant context for downstream flows.

## Non-Functional Requirements

- Tenant selection must be easy to demonstrate on localhost.
- The first slice must avoid custom-domain dependencies.
- The initial onboarding flow must stay bounded and avoid full merchant-administration complexity.

## Constraints

- `merchant-auth-foundation` must complete first.
- The current codebase does not yet have a merchant company model or tenant-aware routing.
- The brownfield app currently centers on shopper flows rather than merchant onboarding.

## Assumptions

- The first slice may use a simple shared signup route rather than one-time invitation tokens.
- The first slice uses path-based tenant routing for local demonstration.
- A shopper account can be reused across tenants in the first implementation if that remains the simpler contract.
- The first merchant onboarding flow only needs one merchant-admin user per company.

## Open Questions

- What minimum merchant-company fields are required beyond company or store name?
- Should merchant onboarding create its first merchant-admin account in the same flow or as a follow-up step?
- When a tenant route is invalid or unknown, should the app redirect, render a not-found experience, or show a tenant-selection screen?

## Risks

- If merchant identity and shopper identity are not separated cleanly, later role and ownership rules will be difficult to add.
- If tenant resolution is inconsistent between browser routing and downstream services, data ownership errors will follow.

## Dependencies

- `merchant-auth-foundation` must establish the user identity contract first.
- The API gateway and frontend routing must agree on how tenant context is represented.
- Downstream product and checkout flows depend on tenant context being resolvable.

## Candidate OpenSpec Changes

- `merchant-tenant-foundation`: create merchant company onboarding, tenant assignment, and browser-visible tenant routing.

## OpenSpec Change Dependencies

- `merchant-tenant-foundation` depends on `merchant-auth-foundation`.

## Recommended Sequencing

- First, complete `merchant-auth-foundation`.
- Second, add merchant onboarding and tenant creation.
- Third, propagate tenant-aware routing into product and checkout experiences.

## Success Metrics

- An internal team member can share a merchant signup link.
- A merchant can create a company or store and receive a tenant context.
- The browser route can switch between at least two tenants in a local demo.
- Tenant context is resolved consistently for tenant-aware requests.