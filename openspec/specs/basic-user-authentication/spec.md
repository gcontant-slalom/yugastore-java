## ADDED Requirements

### Requirement: Email and password registration
The system SHALL allow a user to create an account using email and password and SHALL reject invalid or duplicate registration attempts clearly.

#### Scenario: Valid registration creates a new account
- **WHEN** a user submits a valid email address and a password that meets the minimum password standard
- **THEN** the system creates a new persisted account for that email
- **AND** the stored password value is not persisted in plaintext

#### Scenario: Duplicate email registration is rejected
- **WHEN** a user attempts to register with an email address that already exists
- **THEN** the system rejects the request with a clear duplicate-account outcome

#### Scenario: Invalid email or password is rejected
- **WHEN** a user submits an invalid email address or a password that does not meet the minimum standard
- **THEN** the system rejects the request with clear validation feedback

### Requirement: Email and password login
The system SHALL authenticate a user with valid email and password credentials and SHALL reject invalid login attempts clearly.

#### Scenario: Valid credentials create authenticated state
- **WHEN** a user submits valid email and password credentials
- **THEN** the system authenticates the user
- **AND** the browser-facing authenticated state is established for subsequent requests

#### Scenario: Invalid credentials are rejected
- **WHEN** a user submits an unknown email address or incorrect password
- **THEN** the system rejects the login attempt with a clear invalid-credentials outcome

#### Scenario: Authenticated state persists for subsequent requests
- **WHEN** a user has logged in successfully and then makes a subsequent authenticated request using the supported session or token mechanism
- **THEN** the system recognizes that user as authenticated without requiring re-entry of credentials on the immediate next request

### Requirement: Logout clears authenticated state
The system SHALL allow an authenticated user to log out and SHALL clear the active authenticated state successfully.

#### Scenario: Logout clears active auth state
- **WHEN** an authenticated user invokes logout
- **THEN** the active session or token state is cleared
- **AND** subsequent protected requests from that browser context are treated as unauthenticated until the user logs in again

### Requirement: Auth-critical request path must not rely on hard-coded demo-user identity
The system SHALL use the authenticated user contract for auth-critical requests instead of silently falling back to a hard-coded demo user.

#### Scenario: Protected request resolves current authenticated user
- **WHEN** an authenticated request enters the gateway-facing auth-critical path
- **THEN** the system resolves the current authenticated user from the supported session or token contract

#### Scenario: Unauthenticated protected request is rejected
- **WHEN** a protected auth-critical request arrives without valid authenticated state
- **THEN** the system rejects the request explicitly instead of using the hard-coded demo user