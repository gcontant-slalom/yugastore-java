## Context

The current repository already models product data and sample-data import using checked-in assets, but active planning defers merchant-facing product upload entirely. The desired user journey now requires a merchant to create a tenant at a slug-specific route and then load tenant-owned products so a separate shopper can browse and buy them from that tenant storefront.

This change stays intentionally narrow: it adds the first supported merchant-admin upload path and reuses the same tenant ownership contract that storefront browsing and checkout already need.

## Goals / Non-Goals

**Goals:**
- Let an authenticated merchant-admin upload products into the merchant's own tenant catalog.
- Reuse the canonical tenant slug route and merchant association as the ownership source of truth.
- Return explicit upload feedback for accepted rows, rejected rows, and ownership or validation failures.
- Keep uploaded products compatible with tenant-filtered storefront browsing and tenant-scoped checkout.

**Non-Goals:**
- Full merchant catalog CRUD screens.
- Rich asynchronous import pipelines, background jobs, or warehouse tooling.
- Cross-tenant product sharing.
- Broad inventory management beyond the first upload-owned records.

## Decisions

### 1. Use one bounded CSV upload path in the first slice

Recommended decision: support one CSV-based upload path for the first merchant-admin ingestion flow.

- Why: the repository already uses CSV product assets, so CSV keeps the first upload slice aligned with existing data conventions and avoids inventing a richer ingestion platform prematurely.
- Alternative considered: manual per-product form entry or JSON bulk import.
- Rejected for now because those paths add UI or contract complexity without changing the first business outcome.

### 2. Derive tenant ownership from authenticated merchant context plus slug route

Recommended decision: derive tenant ownership from the authenticated merchant-admin identity and the active slug route, and reject uploads when those inputs do not resolve to the same merchant tenant.

- Why: ownership must be enforceable at the boundary; trusting tenant identifiers inside the uploaded file would weaken isolation.
- Alternative considered: let the file payload declare the tenant slug or tenant id.
- Rejected because it would allow ownership drift and make authorization weaker.

### 3. Keep the first upload contract create-focused with explicit row feedback

Recommended decision: treat the first upload path as create-focused and return row-level acceptance and rejection feedback.

- Why: create-focused behavior is easier to verify and safer than introducing update, merge, or destructive semantics immediately.
- Alternative considered: support upsert or overwrite behavior in the first slice.
- Rejected for now because it expands ambiguity around conflict handling and inventory semantics.

This boundary is fixed for the first slice: edit and update behavior will be specified separately in a later change.

### 4. Persist uploaded ownership in the same model used by storefront filtering

Recommended decision: store uploaded products using the same tenant ownership fields that storefront filtering and tenant-scoped checkout consume.

- Why: uploaded and seeded products must behave identically once stored.
- Alternative considered: keep a separate import staging model.
- Rejected for now because it would duplicate ownership logic and delay end-to-end verification.

## Risks / Trade-offs

- [CSV contract drifts from existing product model] -> Keep the first CSV contract narrow and aligned to the product fields already required for storefront rendering and checkout.
- [Merchant uploads to the wrong tenant] -> Resolve ownership from merchant identity plus slug route and reject mismatches explicitly.
- [Partial failures are hard to debug] -> Return clear row-level results with validation reasons.
- [Create-only behavior may feel limited] -> Keep updates out of scope initially and add them later only if the first upload slice proves valuable.
- [Edit requests appear before the first slice is stable] -> Hold edit semantics for a separate change instead of widening the initial upload contract.

## Migration Plan

- Add the merchant upload route and API contract after canonical slug routing is in place.
- Define the first CSV schema and row validation outcomes.
- Persist uploaded products and ownership metadata in the products service.
- Make storefront reads and checkout consume uploaded products through the existing tenant filtering contract.
- Add focused verification for upload success, upload rejection, tenant isolation, and shopper purchase behavior using uploaded products.
- If rollback is needed, disable the upload entry point and preserve stored tenant ownership rules.

## Open Questions

- Which exact product fields must be mandatory in the first CSV contract?
- Does the first upload slice need inventory quantity in the same file, or can inventory stay on an existing default path for the first iteration?
- Should the upload entry point live under a tenant admin route in the UI, or remain API-first until merchant UI work is implemented?