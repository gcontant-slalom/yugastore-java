# Checkout Second Product Fix

## Summary

Fix HTTP 500 (Internal Server Error) that occurs when checking out the second most popular product (Latin dictionary) while first product checkout succeeds.

## Scope

**What:** Diagnose and fix the root cause of the 500 error in checkout-microservice for product ASIN-2 (Latin dictionary).

**Affected Services:** checkout-microservice  
**Affected Code Paths:** checkout submission, price calculation, inventory lookup, order creation  

**What's NOT included:**
- Broad refactoring of cart or checkout
- API contract changes
- Schema changes (unless required by root cause)
- UI changes

## Impact

- Unblocks customer purchase of Latin dictionary
- Ensures all products can be checked out consistently
- Restores availability of the second product in the storefront

## Success Criteria

- Checkout succeeds for Latin dictionary product
- No regression on first product
- 500 error is resolved or replaced with meaningful error
- Root cause is documented

## Key Constraints

- No API contract changes
- Reproduce and test on local dev only (Docker and deployed environments later if needed)
- No schema changes unless necessary

## Reference

Source PRD: [resources/prds/checkout-bug-fix.md](../../../../resources/prds/checkout-bug-fix.md)

## Pre-Implementation Requirement

**Blocker:** Stack trace or detailed error logs from the 500 response must be captured before implementation can proceed. This will guide root cause diagnosis and fix direction.
