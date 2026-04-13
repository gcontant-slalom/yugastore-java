# Development Guidelines

## Scope and Workflow

- Prefer editing existing files over creating new ones. Create new files only for genuinely new features, required tests, or necessary configuration.
- Keep changes scoped to the affected module unless the task clearly requires coordinated updates across services.
- Match the existing code patterns in the touched area. Follow the same naming conventions, error handling style, package structure, and architectural decisions already present in the project.
- When refactoring, make small incremental changes. Do not mix refactoring with feature work unless the task explicitly requires both.
- Use `./mvnw` for Maven commands instead of relying on a globally installed Maven.

## Code Quality

- Generate code that handles edge cases and error conditions. Validate inputs early and fail fast for invalid state.
- Include null and missing-value checks where they are appropriate for the surrounding code.
- Use clear, descriptive names for variables, methods, classes, and components.
- Keep functions and methods focused on a single responsibility.
- Avoid deep nesting. Prefer guard clauses and early returns when they improve readability.
- Extract complex logic into well-named helper methods instead of embedding unnecessary inline complexity.
- Remove unused code, dead branches, and unused imports.
- Do not prematurely optimize. Fix obvious performance problems such as repeated unnecessary queries or avoidable loops, but do not trade away readability for speculative gains.
- Never include trailing spaces or tabs on blank lines.

## Error Handling and Security

- Handle errors at the appropriate level and return useful error messages.
- Do not swallow exceptions silently.
- Validate and sanitize user input.
- Use parameterized queries and safe data access patterns to avoid SQL injection.
- Never include secrets, API keys, passwords, or credentials in source code, tests, or configuration.

## Testing

- Prefer focused, readable, deterministic tests.
- Keep tests independent and fast.
- Avoid tests that require external services unless the dependency is explicitly part of the task and is appropriately isolated or mocked.
- Suggest or add tests for new features, bug fixes, complex logic, public APIs, and critical paths.
- Do not add tests for trivial getters, setters, or wiring-only code unless the project already tests that pattern.
- In this repository, prefer targeted verification for the changed module, such as `./mvnw -pl cart-microservice test`.

## Project-Specific Rules

- Preserve existing request and response contracts, service names, ports, and Eureka discovery behavior unless the task explicitly changes them.
- Confirm the database API before changing persistence logic.
- `products-microservice` and `checkout-microservice` use YCQL.
- `cart-microservice` and `login-microservice` use YSQL or PostgreSQL-compatible access.
- Keep frontend changes inside `react-ui/frontend` unless backend or gateway changes are explicitly required.
- Prefer the checked-in schema and sample data assets under `resources/` when changing local database setup.

## Documentation

- Do not create documentation files unless explicitly requested.
- Add documentation for public APIs, complex algorithms, non-obvious implementation choices, and important workarounds.
- Skip documentation that only restates obvious code.
- Follow the project’s existing style and use automated formatters when available.