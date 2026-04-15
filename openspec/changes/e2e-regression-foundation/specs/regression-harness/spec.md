## ADDED Requirements

### Requirement: Regression harness bootstraps a deterministic local environment
The system SHALL provide a supported regression bootstrap path that prepares the documented prerequisites, startup assumptions, and seeded application state required for end-to-end validation.

#### Scenario: Bootstrap prepares supported prerequisites
- **WHEN** a maintainer invokes the supported regression bootstrap for Yugastore
- **THEN** the harness verifies the required runtime prerequisites and startup dependencies before executing end-to-end tests

#### Scenario: Bootstrap fails with actionable diagnostics
- **WHEN** a required dependency or seeded-data prerequisite is missing during bootstrap
- **THEN** the harness stops before running the suite and reports which prerequisite blocked execution

### Requirement: Regression harness exposes stable smoke and full-suite commands
The system SHALL define stable command scopes for at least smoke and full regression execution.

#### Scenario: Smoke scope runs the fast supported checks
- **WHEN** a maintainer invokes the supported smoke regression command
- **THEN** the harness executes the subset of end-to-end checks designated for routine validation

#### Scenario: Full scope runs the broader supported checks
- **WHEN** a maintainer invokes the supported full regression command
- **THEN** the harness executes the broader end-to-end suite designated for complete local regression validation

### Requirement: Regression execution preserves actionable failure artifacts
The system SHALL preserve enough diagnostic output for a maintainer to triage startup, data, routing, and assertion failures.

#### Scenario: Assertion failure produces retained artifacts
- **WHEN** an end-to-end regression check fails after the harness starts successfully
- **THEN** the harness preserves the failure artifacts and summary needed to investigate the regression

#### Scenario: Passing run exits cleanly
- **WHEN** the supported regression scope completes without failures
- **THEN** the harness exits successfully and reports that the selected scope passed

### Requirement: Regression runs reset supported test state predictably
The system SHALL reset or recreate supported test state so repeated executions start from a predictable baseline.

#### Scenario: Repeated runs begin from the same supported baseline
- **WHEN** a maintainer runs the same supported regression scope multiple times against the supported environment
- **THEN** each run begins from the same documented seeded or reset application state
