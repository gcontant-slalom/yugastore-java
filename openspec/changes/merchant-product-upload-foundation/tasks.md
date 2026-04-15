This change starts only after canonical tenant slug routing is available through `merchant-tenant-foundation`.

## 1. Upload Contract

- [ ] 1.1 Define the first supported create-only product upload contract, including file format, required fields, duplicate-product handling, and upload result shape.
- [ ] 1.2 Define merchant-admin authorization and tenant-ownership checks for uploads bound to the active slug route.

## 2. Upload Implementation

- [ ] 2.1 Add the merchant upload entry point in `react-ui` and `api-gateway-microservice` for the first bounded upload flow.
- [ ] 2.2 Implement create-only upload parsing, validation, duplicate-product rejection, and tenant-owned product persistence in `products-microservice`.
- [ ] 2.3 Reject unauthenticated uploads and merchant-to-tenant mismatch uploads explicitly.

## 3. Storefront And Purchase Integration

- [ ] 3.1 Ensure uploaded products participate in the existing tenant-filtered storefront reads.
- [ ] 3.2 Verify tenant-scoped checkout behavior for products created through the upload flow.

## 4. Verification

- [ ] 4.1 Add focused tests for upload success, validation failures, unauthorized upload rejection, and cross-tenant isolation.
- [ ] 4.2 Update local demo guidance to include the first merchant product upload flow.