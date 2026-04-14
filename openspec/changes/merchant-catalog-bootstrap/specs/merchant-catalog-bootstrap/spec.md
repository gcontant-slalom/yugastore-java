## ADDED Requirements

### Requirement: Tenant-owned starter catalogs are bootstrapable from seed assets
The system SHALL support tenant-owned starter catalogs through checked-in seed assets for the first slice.

#### Scenario: Seed workflow creates distinct tenant catalogs
- **WHEN** the sample-data workflow initializes starter catalog data for the first slice
- **THEN** the resulting data contains at least two tenants with distinct product assortments

#### Scenario: Seeded products carry tenant ownership
- **WHEN** a product record is created through the supported bootstrap workflow
- **THEN** the product record stores the tenant ownership required to identify its merchant context

### Requirement: Storefront catalog browsing is filtered by the active tenant
The system SHALL show storefront products for the active tenant only.

#### Scenario: Active tenant route returns only matching products
- **WHEN** a shopper browses the storefront with a resolved active tenant context
- **THEN** the product results shown to that shopper contain only products owned by that tenant

#### Scenario: Switching tenants changes the visible catalog
- **WHEN** a shopper switches from one supported tenant route to another
- **THEN** the storefront updates to the product assortment owned by the newly active tenant

### Requirement: The first shopping flow is tenant-scoped end to end
The system SHALL keep the first cart and checkout flow scoped to the active tenant context.

#### Scenario: Cart or checkout request carries active tenant ownership
- **WHEN** a shopper adds a product to the first supported shopping flow from a resolved tenant context
- **THEN** the resulting targeted cart or checkout operation uses that same tenant context

#### Scenario: Missing tenant context is rejected in the shopping flow
- **WHEN** a targeted cart or checkout request arrives without resolvable tenant context
- **THEN** the system rejects the request explicitly instead of processing it as a global or demo-user flow