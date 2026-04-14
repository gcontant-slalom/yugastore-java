## ADDED Requirements

### Requirement: Merchant company and store ownership model
The system SHALL represent merchant company or store ownership separately from user identity for the initial white-label foundation. The model SHALL allow multiple users to belong to the same merchant company or store.

#### Scenario: Multiple users share one merchant context
- **WHEN** the platform stores or resolves merchant-admin identities for the same merchant organization
- **THEN** those identities are associated to one shared merchant company or store context rather than being treated as separate tenants

#### Scenario: Merchant context is distinct from a user identifier
- **WHEN** the system creates or updates tenant-aware merchant-owned records
- **THEN** those records use merchant company or store ownership data that is independent of any hard-coded demo user value

### Requirement: Tenant context propagation for merchant-owned requests
The system SHALL resolve and propagate tenant context for merchant-owned operations on the targeted request path instead of relying on hard-coded demo-user assumptions.

#### Scenario: Gateway passes tenant context downstream
- **WHEN** a tenant-aware merchant request enters the targeted request path
- **THEN** the API gateway passes merchant company or store context to the downstream service calls touched by this change

#### Scenario: Missing tenant context is rejected explicitly
- **WHEN** a merchant-owned endpoint touched by this change receives a request without resolvable tenant context
- **THEN** the system rejects the request explicitly instead of silently falling back to a hard-coded demo user

### Requirement: Tenant ownership is persisted on targeted merchant-owned records
The system SHALL persist merchant company or store ownership on the targeted merchant-owned records created or updated by this change.

#### Scenario: Product data is created with ownership metadata
- **WHEN** a tenant-aware product operation creates or updates a product record in the first slice
- **THEN** the resulting record stores merchant company or store ownership metadata

#### Scenario: Checkout data carries the same merchant context
- **WHEN** a tenant-aware checkout operation creates or updates a targeted checkout record in the first slice
- **THEN** the resulting record stores the merchant company or store ownership associated with the request context