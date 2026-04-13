---
description: "Use when working with YugabyteDB in yugastore-java, including YCQL or YSQL schema changes, data loading, database configuration, query logic, and backend code that reads or writes YugabyteDB. Covers service-to-database mapping, repo-specific setup, and safe database workflow."
name: "YugabyteDB Guidance"
applyTo: "resources/**/*.cql, resources/**/*.sql, resources/**/*.csv, resources/**/*.sh, ycqlsh.sh"
---
# YugabyteDB Guidance

- Confirm which YugabyteDB API the touched service uses before changing queries, repositories, drivers, or schema assumptions.
- `products-microservice` and `checkout-microservice` use YCQL.
- `cart-microservice` and `login-microservice` use YSQL/PostgreSQL-compatible access.
- Do not mix YCQL and YSQL patterns in the same service unless the task explicitly requires coordinated changes.
- Prefer the checked-in schema and seed assets under `resources/` when updating database setup or sample data.
- For local data loading, prefer the documented `ycqlsh COPY` workflow over the legacy `cassandra-loader` path.
- Keep schema changes minimal and aligned with the existing sample app contracts. If a schema change affects multiple services, call out the dependency explicitly.
- When changing backend database logic, verify the owning service configuration and repository layer match the correct YugabyteDB API.
- If validation requires a running YugabyteDB instance, state that clearly instead of implying full verification.
- Do not edit generated output under `target/` or replace the current database access approach with a different framework unless the user asks for it.