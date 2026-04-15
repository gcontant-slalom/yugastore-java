## ADDED Requirements

### Requirement: Regression suite covers supported availability and storefront smoke behavior
The system SHALL automate smoke checks that prove the supported application stack is reachable and the storefront can load seeded catalog data.

#### Scenario: Smoke scope confirms supported application availability
- **WHEN** the smoke regression scope runs against the supported environment
- **THEN** it verifies the supported application entrypoints required for current-state validation are reachable

#### Scenario: Smoke scope verifies storefront catalog visibility
- **WHEN** the smoke regression scope exercises the storefront with supported seeded data
- **THEN** it verifies that the storefront can display the expected catalog state for the current supported flow

### Requirement: Regression suite covers the supported merchant onboarding or tenant-context path
The system SHALL automate the merchant onboarding or tenant-context journey that is currently supported and demonstrable in the repository.

#### Scenario: Supported onboarding path succeeds end to end
- **WHEN** the regression suite executes the supported merchant onboarding or tenant-context journey
- **THEN** the application completes that journey using only currently supported behavior

#### Scenario: Supported session re-entry preserves expected tenant visibility
- **WHEN** the regression suite logs out and re-enters the supported merchant or tenant flow
- **THEN** the expected tenant-context behavior remains observable after re-entry

### Requirement: Regression suite covers the supported cart and checkout journey
The system SHALL automate the current supported add-to-cart and checkout journey used for whole-application validation.

#### Scenario: Supported shopper journey completes cart and checkout behavior
- **WHEN** the regression suite executes the supported shopper purchase path with seeded catalog data
- **THEN** the application completes the supported cart and checkout flow without regression

#### Scenario: Checkout regression surfaces as a failed end-to-end journey
- **WHEN** the supported cart or checkout behavior changes incompatibly
- **THEN** the automated regression journey fails explicitly rather than passing silently

### Requirement: Critical journeys are mapped to regression scopes intentionally
The system SHALL classify critical journey tests into documented regression scopes.

#### Scenario: Journey metadata identifies scope membership
- **WHEN** a maintainer reviews the supported end-to-end regression suite
- **THEN** each critical journey identifies whether it belongs to smoke, full, or both scopes
