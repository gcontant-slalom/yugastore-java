# Specification: Checkout 500 Error Resolution for Latin Dictionary

## Current Behavior

- Attempting checkout with the first product (ASIN-1) → HTTP 200 success response and order created
- Attempting checkout with the second product (ASIN-2, Latin dictionary) → HTTP 500 Internal Server Error
- Error is reproducible on local dev environment with current seed data

## Expected Behavior

- Checkout with any product, including ASIN-2 (Latin dictionary) → HTTP 200 success response and order created
- No 500 error
- Order state is persisted consistently

## Acceptance Criteria

1. Checkout endpoint accepts cart with Latin dictionary product and returns HTTP 200
2. Order record is created in database
3. Inventory is deducted correctly
4. Price is calculated correctly
5. First product checkout continues to work (no regression)

## Functional Changes

- **Checkout Endpoint**: Fix the bug that causes 500 error for ASIN-2
- **Error Path**: Replace 500 with meaningful error message if the root cause is user-data related (e.g., invalid price, missing SKU)
- **Logging**: Add logging to diagnose future similar issues

## API Changes

- No changes to request/response contracts
- HTTP status remains 200 on success, 4xx for validation errors, 5xx only for unrecoverable infrastructure issues

## Data Model Changes

- None required (unless root cause necessitates)

## Testing Scope

- Local dev environment with current seed data
- Manual test: add first product → checkout (pass); add second product → checkout (should now pass)
- Verify no regression on first product after fix
