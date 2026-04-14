## 1. Auth Contract and Data Model

- [ ] 1.1 Define the gateway-facing auth contract for register, login, logout, and any required current-user lookup behavior.
- [ ] 1.2 Update the YSQL-backed user model and schema to support unique email-based accounts and secure password hashes for the first auth slice.

## 2. Backend Authentication Implementation

- [ ] 2.1 Implement registration validation, duplicate-email handling, and secure password hashing in `login-microservice`.
- [ ] 2.2 Implement login credential validation and the chosen browser-facing authenticated-state mechanism.
- [ ] 2.3 Implement logout behavior that clears the active session or token state and stops protected flows from falling back to the hard-coded demo user.

## 3. Gateway and Frontend Integration

- [ ] 3.1 Update `api-gateway-microservice` so the React app uses a consistent auth boundary for register, login, logout, and protected requests.
- [ ] 3.2 Add React UI flows for registration, login, logout, and authenticated or unauthenticated state handling.

## 4. Verification and Sequencing

- [ ] 4.1 Add focused tests for registration, duplicate-email rejection, login, invalid-credential handling, and logout.
- [ ] 4.2 Record that tenant, company, onboarding, and advanced authorization work remain blocked until this auth foundation is complete.