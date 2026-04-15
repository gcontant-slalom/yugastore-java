## ADDED Requirements

### Requirement: Merchant company signup creates tenant context
The system SHALL expose a merchant company signup entry point at a tenant-specific slug path that creates a merchant company or store and a corresponding tenant context for the first slice.

#### Scenario: Shared merchant signup entry point starts onboarding
- **WHEN** an internal team member shares a supported merchant signup link such as `/my-test-store/signup` with a prospective merchant
- **THEN** the merchant can open a dedicated onboarding route for company or store signup

#### Scenario: Successful merchant signup creates tenant context
- **WHEN** a merchant submits the required company or store details and requested slug through the onboarding entry point
- **THEN** the system creates a merchant company or store record
- **AND** the system creates a unique tenant context associated with that merchant
- **AND** the resulting tenant is bound to the requested slug when that slug passes validation

#### Scenario: Duplicate or invalid requested slug is rejected
- **WHEN** a merchant submits a signup request with a slug that is invalid or already assigned to another tenant
- **THEN** the system rejects the signup request with a clear invalid-slug or duplicate-slug outcome

### Requirement: Merchant company or store identity is distinct from shopper identity
The system SHALL represent merchant company or store ownership separately from shopper identity for the initial white-label foundation.

#### Scenario: Merchant identity is stored independently from shopper identity
- **WHEN** the system creates or updates tenant-aware merchant-owned records
- **THEN** those records use merchant company or store ownership data that is independent of shopper identity data

#### Scenario: Shared merchant context can map to more than one user
- **WHEN** the platform resolves merchant-admin identities for the same merchant organization
- **THEN** those identities can be associated to one shared merchant company or store context rather than being treated as separate tenants

### Requirement: Path-based tenant route resolves tenant context
The system SHALL resolve tenant context from canonical browser slug routes for the first slice.

#### Scenario: Shared root storefront remains distinct from tenant storefronts
- **WHEN** a browser accesses `/`
- **THEN** the application serves the demo store behavior
- **AND** the request is not treated as a tenant storefront route by default

#### Scenario: Supported tenant path resolves the active tenant
- **WHEN** a shopper or merchant accesses a supported tenant storefront path such as `/my-test-store/` or tenant signup path such as `/my-test-store/signup`
- **THEN** the application resolves the corresponding tenant context for downstream requests in the targeted flow

#### Scenario: Unknown tenant path is rejected explicitly
- **WHEN** the application receives a canonical tenant route whose tenant slug cannot be resolved
- **THEN** the request is rejected or routed to a clear invalid-tenant outcome instead of falling back silently

### Requirement: Tenant slugs use a stable browser-safe contract
The system SHALL use tenant slugs as stable browser-visible identifiers for the first slice.

#### Scenario: Tenant slug uses supported format
- **WHEN** the system accepts a tenant slug for creation
- **THEN** the slug uses the supported browser-safe format for the first slice
- **AND** the stored slug can be used directly in storefront and signup URLs

#### Scenario: Tenant slug remains unique
- **WHEN** the platform stores a tenant slug
- **THEN** no other tenant can be created with that same slug

### Requirement: Tenant context propagates to targeted merchant-owned operations
The system SHALL resolve and propagate tenant context for merchant-owned operations on the targeted request path instead of relying on hard-coded demo-user assumptions.

#### Scenario: Gateway passes tenant context downstream
- **WHEN** a tenant-aware merchant or storefront request enters the targeted request path
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