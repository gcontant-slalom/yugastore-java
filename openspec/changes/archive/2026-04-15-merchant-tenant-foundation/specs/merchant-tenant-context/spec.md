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

#### Scenario: Successful merchant signup assigns the current authenticated user as first merchant-admin
- **WHEN** an already authenticated user successfully completes merchant signup for a new tenant
- **THEN** the system assigns that same authenticated user to the tenant as its first merchant-admin membership in the same flow
- **AND** the flow does not require a separate follow-up user-creation step to establish the initial merchant-admin

#### Scenario: Duplicate or invalid requested slug is rejected
- **WHEN** a merchant submits a signup request with a slug that is invalid or already assigned to another tenant
- **THEN** the system rejects the signup request with a clear invalid-slug or duplicate-slug outcome

### Requirement: Merchant onboarding success state is transient
The system SHALL show merchant onboarding success confirmation only for the successful tenant-creation flow that just completed.

#### Scenario: Successful signup shows current-flow confirmation
- **WHEN** a merchant successfully creates a tenant from the onboarding form
- **THEN** the onboarding experience shows a success confirmation for that completed submission
- **AND** the confirmation identifies the created tenant in the current flow

#### Scenario: Success confirmation clears after leaving and returning
- **WHEN** a merchant sees the onboarding success confirmation and then leaves the onboarding page or returns to it later without submitting the form again
- **THEN** the old success confirmation is not shown on the returned onboarding page
- **AND** the page presents a clean onboarding form state plus any durable tenant data the product intentionally exposes

#### Scenario: Success confirmation does not survive a fresh page load
- **WHEN** a merchant reloads the onboarding page after a previously successful tenant creation without performing another submit
- **THEN** the old success confirmation is not restored from client-side persisted state

### Requirement: Merchant company or store identity is distinct from shopper identity
The system SHALL represent merchant company or store ownership separately from shopper identity for the initial white-label foundation.

#### Scenario: Merchant identity is stored independently from shopper identity
- **WHEN** the system creates or updates tenant-aware merchant-owned records
- **THEN** those records use merchant company or store ownership data that is independent of shopper identity data

#### Scenario: Shared merchant context can map to more than one user
- **WHEN** the platform resolves merchant-admin identities for the same merchant organization
- **THEN** those identities can be associated to one shared merchant company or store context rather than being treated as separate tenants

#### Scenario: Login service persists tenant and membership foundation without expanding auth scope
- **WHEN** the first-slice merchant tenant foundation is persisted in `login-microservice`
- **THEN** the YSQL-backed model remains bounded to tenant records, tenant membership records, and the existing merchant-context response contract
- **AND** the change does not introduce a new standalone authentication or invitation flow inside `login-microservice`

#### Scenario: First-slice merchant context uses the bounded tenant response shape
- **WHEN** the platform creates or resolves merchant context in this first slice
- **THEN** the returned merchant context identifies the tenant with `tenantId`, `tenantKey`, and `companyName`
- **AND** the current merchant-admin user is exposed separately from the tenant as `merchantAdminUserId`

### Requirement: Path-based tenant route resolves tenant context
The system SHALL resolve tenant context from canonical browser slug routes for the first slice.

#### Scenario: Shared root storefront remains distinct from tenant storefronts
- **WHEN** a browser accesses `/`
- **THEN** the application serves the demo store behavior
- **AND** the request is not treated as a tenant storefront route by default
- **AND** the existing `cronos` sample dataset is treated as the default merchant context for that shared root storefront

#### Scenario: Supported tenant path resolves the active tenant
- **WHEN** a shopper or merchant accesses a supported tenant storefront path such as `/my-test-store/` or tenant signup path such as `/my-test-store/signup`
- **THEN** the application resolves the corresponding tenant context for downstream requests in the targeted flow

#### Scenario: Canonical tenant browser routes use one public gateway slug lookup
- **WHEN** `react-ui` receives a canonical tenant browser route for `/{tenantSlug}/` or `/{tenantSlug}/signup`
- **THEN** tenant resolution uses the public gateway merchant-context lookup keyed by that slug
- **AND** the shared root path `/` does not require tenant lookup

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

#### Scenario: Gateway keeps tenant context distinct from authenticated user identity
- **WHEN** the gateway resolves tenant context for a tenant-aware request in the first bounded flow
- **THEN** it forwards merchant tenant context separately from any authenticated user identity already used by other contracts
- **AND** downstream services touched by this change do not need to infer tenant ownership from a hard-coded demo user

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

## Notes

### Verification inventory for task 5.1

- Merchant onboarding routing and slug-resolution coverage is exercised in `login-microservice`, `api-gateway-microservice`, and `react-ui` so canonical tenant paths and unknown-tenant outcomes remain observable.
- Tenant-context propagation coverage is exercised in `products-microservice`, `api-gateway-microservice`, and `react-ui`, including the public gateway lookup and the downstream headers used on the targeted request path.
- Ownership persistence coverage is exercised in `login-microservice`, `checkout-microservice`, and `cart-microservice` so the first-slice tenant foundation is validated at the records that now carry merchant ownership metadata.
- Missing-context rejection coverage is exercised in `cart-microservice`, `api-gateway-microservice`, and `react-ui` so tenant-owned flows fail explicitly instead of silently falling back to the shared demo context.