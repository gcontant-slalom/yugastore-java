## ADDED Requirements

### Requirement: Merchant-admin users can upload products into their tenant catalog
The system SHALL allow an authenticated merchant-admin user to upload products into the merchant's own tenant catalog through the first supported file-based upload path.

#### Scenario: Authenticated merchant uploads products successfully
- **WHEN** an authenticated merchant-admin submits a valid product upload for the active tenant slug route
- **THEN** the system creates tenant-owned product records for that merchant tenant
- **AND** the upload result reports which rows were accepted

#### Scenario: Unauthenticated upload is rejected
- **WHEN** a product upload request arrives without valid authenticated merchant-admin state
- **THEN** the system rejects the upload explicitly

### Requirement: Upload ownership is derived from merchant tenant context
The system SHALL derive uploaded product ownership from the authenticated merchant association and active tenant slug context rather than trusting tenant ownership values inside the uploaded file.

#### Scenario: Merchant and active slug resolve to one tenant
- **WHEN** the authenticated merchant-admin and active slug route resolve to the same tenant
- **THEN** uploaded product records are stored with that tenant ownership

#### Scenario: Merchant and active slug do not match
- **WHEN** an authenticated merchant-admin submits an upload against a slug route that is not associated with that merchant tenant
- **THEN** the system rejects the upload with a clear tenant-ownership mismatch outcome

### Requirement: Upload validation returns explicit row outcomes
The system SHALL return explicit upload feedback for accepted rows and rejected rows in the first upload slice.

#### Scenario: Upload contains invalid rows
- **WHEN** an authenticated merchant-admin uploads a file that contains one or more invalid product rows
- **THEN** the system returns a result that identifies rejected rows and their validation failures

#### Scenario: Upload contains valid rows
- **WHEN** an authenticated merchant-admin uploads a file with valid product rows
- **THEN** the system returns a result that identifies the accepted rows

#### Scenario: Upload reuses an existing product key for the same tenant
- **WHEN** an authenticated merchant-admin uploads a row whose product key already exists for that tenant
- **THEN** the system rejects that row with a clear duplicate-product outcome

### Requirement: The first upload flow is create-only
The system SHALL treat the first merchant-admin upload capability as create-only and SHALL not modify existing tenant-owned product records.

#### Scenario: Upload attempts to modify an existing tenant product
- **WHEN** an authenticated merchant-admin submits upload data that matches an existing tenant-owned product record
- **THEN** the system does not overwrite the existing product record
- **AND** the upload result reports that row as rejected for the create-only flow

### Requirement: Uploaded products follow the same tenant storefront isolation rules
The system SHALL expose uploaded products only through the matching tenant storefront and tenant-scoped purchase flow.

#### Scenario: Shopper sees uploaded products only in the matching tenant storefront
- **WHEN** a shopper opens the storefront for the tenant slug that owns uploaded products
- **THEN** the storefront can include those uploaded products in the visible catalog

#### Scenario: Shopper cannot see uploaded products from another tenant storefront
- **WHEN** a shopper opens a different tenant storefront
- **THEN** uploaded products owned by another tenant are not returned in that storefront catalog