## ADDED Requirements

### Requirement: Covered work triggers relevant regression execution
The repository workflow SHALL require the relevant supported regression scope when a change touches modules or journeys covered by the end-to-end regression suite.

#### Scenario: Covered change runs the relevant regression scope
- **WHEN** a maintainer or Copilot session changes code in an area covered by the supported end-to-end regression suite
- **THEN** the workflow requires running the relevant documented regression scope before the work is considered complete

#### Scenario: Unavailable regression run is reported explicitly
- **WHEN** the relevant regression scope cannot be executed because of a documented blocker
- **THEN** the workflow reports the blocker explicitly instead of implying that regression verification succeeded

### Requirement: Regression test modifications require explicit rationale and confirmation
The repository workflow SHALL require explicit rationale and confirmation before an existing regression test is relaxed, removed, or materially repurposed.

#### Scenario: Regression test update states why behavior changed
- **WHEN** a maintainer proposes changing an existing regression test
- **THEN** the workflow records a clear explanation of the product change, bug fix, or test correction that justifies the update

#### Scenario: Regression test weakening requires explicit confirmation
- **WHEN** a maintainer proposes relaxing or removing an existing regression assertion or journey
- **THEN** the workflow requires explicit confirmation instead of allowing the change to proceed silently

### Requirement: Workflow output reports regression verification status clearly
The repository workflow SHALL make regression verification status visible in agent output.

#### Scenario: Successful regression execution is reported
- **WHEN** the relevant supported regression scope completes successfully
- **THEN** the workflow reports which regression scope ran and that it passed

#### Scenario: Blocked or partial verification is reported
- **WHEN** only partial regression verification is possible for a covered change
- **THEN** the workflow reports what ran, what did not run, and why the remaining verification was blocked or skipped
