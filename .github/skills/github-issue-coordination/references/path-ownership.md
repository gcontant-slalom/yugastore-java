# Path Ownership

Use module-level path prefixes as the default reservation units for this monorepo.

## Default Ownership Map
- `login-microservice/src/main/**` and `resources/schema.sql`
- `products-microservice/src/main/**`, `checkout-microservice/src/main/**`, and `resources/schema.cql`
- `api-gateway-microservice/src/main/**` and `react-ui/frontend/src/**`

## Shared Paths
These paths should be treated as coordinated work and should not be changed under an isolated agent lock unless the issue explicitly reserves them:

- `pom.xml`
- `.github/**`
- `docs/**`
- `resources/**` other than the issue-specific schema file

## Locking Rule
- Reserve path prefixes, not single files, whenever possible.
- Two open issues labeled `agent-locked` must not own overlapping path prefixes.
- If one issue depends on another, wait for the upstream lock to clear before claiming the downstream issue.
