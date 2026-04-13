---
description: "Use when editing Java, Spring Boot, Maven, or service configuration files in yugastore-java. Covers microservice boundaries, YugabyteDB API choices, testing scope, and repo-specific backend workflow."
name: "Java Service Guidance"
applyTo: "**/src/main/java/**/*.java, **/src/test/java/**/*.java, **/pom.xml, **/application.yml, **/application-*.yml"
---
# Java Service Guidance

- Keep backend changes scoped to the owning microservice unless the task explicitly requires coordinated updates.
- Preserve existing endpoint shapes, request parameters, service names, ports, and Eureka discovery behavior unless the user asks for a contract change.
- Follow the package layout and Spring patterns already used in the touched module instead of introducing a new architecture style.
- Confirm the storage API before changing data access: `products-microservice` and `checkout-microservice` use YCQL, while `cart-microservice` and `login-microservice` use YSQL/PostgreSQL-compatible access.
- Do not invent missing login or authentication flows in `login-microservice`; it is incomplete unless the user explicitly asks to extend it.
- Keep configuration edits local to the owning service and update related docs only when startup behavior, ports, or schema/data setup changes.
- Use `./mvnw`, not a globally installed Maven. Prefer targeted verification such as `./mvnw -pl cart-microservice test`.
- Existing tests are mostly context-load tests. Add focused tests for changed business logic when practical, but do not broaden the test surface unnecessarily.
- Do not edit generated output under `target/` or make incidental dependency or framework upgrades.
