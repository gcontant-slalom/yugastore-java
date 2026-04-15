const fs = require('fs');
const os = require('os');
const path = require('path');
const {
  copySeedFileToDocker,
  countCsvRows,
  createRunContext,
  detectDatabaseMode,
  loadJson,
  parseArgs,
  parseCount,
  repoRoot,
  runCommand,
  writeRunSummary,
  ycqlCommand,
  ysqlCommand
} = require('./shared.cjs');

function runOrThrow(result, message) {
  if (result.status === 0) {
    return result;
  }

  throw new Error(`${message}\n${result.stderr || result.stdout}`.trim());
}

function schemaResultIsIgnorable(result) {
  const output = `${result.stdout || ''}\n${result.stderr || ''}`;
  return /already exists|Duplicate Object/i.test(output);
}

function runSchemaCommand(result, message) {
  if (result.status === 0 || schemaResultIsIgnorable(result)) {
    return result;
  }

  throw new Error(`${message}\n${result.stderr || result.stdout}`.trim());
}

function copyStatement(seed, sourcePath) {
  return `COPY ${seed.table} (${seed.columns}) FROM '${sourcePath}';`;
}

function runSeedCommand(databaseMode, statement) {
  if (databaseMode.kind === 'docker') {
    return ycqlCommand(databaseMode, ['-f', '-'], { input: statement });
  }

  const tempDirectory = fs.mkdtempSync(path.join(os.tmpdir(), 'yugastore-e2e-'));
  const tempFile = path.join(tempDirectory, 'seed.cql');
  fs.writeFileSync(tempFile, `${statement}\n`);

  try {
    return ycqlCommand(databaseMode, ['-f', tempFile]);
  } finally {
    fs.rmSync(tempDirectory, { recursive: true, force: true });
  }
}

async function runReset(options = {}) {
  const scope = options.scope || 'adhoc';
  const runContext = options.runContext || createRunContext(scope);
  const summaryName = options.summaryName || 'reset';
  const servicesConfig = loadJson('e2e/config/services.json');
  const baseline = loadJson('e2e/fixtures/baseline.json');
  const steps = [];
  const notes = [];

  const databaseMode = detectDatabaseMode(servicesConfig);
  if (!databaseMode) {
    const summary = {
      scope,
      ok: false,
      command: 'reset',
      summaryName,
      steps: [
        {
          name: 'database-mode',
          ok: false,
          detail: 'Unable to find a running `yugastore-yb` container or a local ycqlsh/ysqlsh pair for reset execution'
        }
      ],
      notes: [
        'Start the YugabyteDB Docker container named `yugastore-yb`, or install both `ycqlsh` and `ysqlsh` locally before rerunning reset.'
      ]
    };
    writeRunSummary({
      ...runContext,
      summaryJson: path.join(runContext.runDir, `${summaryName}-summary.json`),
      summaryText: path.join(runContext.runDir, `${summaryName}-summary.txt`)
    }, 'Yugastore E2E Reset', summary);
    return summary;
  }

  steps.push({
    name: 'database-mode',
    ok: true,
    detail: databaseMode.kind === 'docker' ? `Using Docker container ${databaseMode.container}` : 'Using local ycqlsh/ysqlsh commands'
  });

  try {
    const ycqlSchemaPath = path.join(repoRoot, baseline.ycql.schemaFile);
    const ycqlSchema = fs.readFileSync(ycqlSchemaPath, 'utf8');
    const ycqlSchemaResult = databaseMode.kind === 'docker'
      ? ycqlCommand(databaseMode, ['-f', '-'], { input: ycqlSchema })
      : ycqlCommand(databaseMode, ['-f', ycqlSchemaPath]);
    runSchemaCommand(
      ycqlSchemaResult,
      'Unable to apply resources/schema.cql'
    );
    steps.push({
      name: 'apply-ycql-schema',
      ok: true,
      detail: ycqlSchemaResult.status === 0
        ? `Applied ${baseline.ycql.schemaFile}`
        : `Applied ${baseline.ycql.schemaFile} with existing-object warnings` 
    });

    const ysqlSchemaPath = path.join(repoRoot, baseline.ysql.schemaFile);
    const ysqlSchema = fs.readFileSync(ysqlSchemaPath, 'utf8');
    const ysqlSchemaResult = databaseMode.kind === 'docker'
      ? ysqlCommand(databaseMode, servicesConfig, ['-f', '-'], { input: ysqlSchema })
      : ysqlCommand(databaseMode, servicesConfig, ['-f', ysqlSchemaPath]);
    runSchemaCommand(
      ysqlSchemaResult,
      'Unable to apply resources/schema.sql'
    );
    steps.push({
      name: 'apply-ysql-schema',
      ok: true,
      detail: ysqlSchemaResult.status === 0
        ? `Applied ${baseline.ysql.schemaFile}`
        : `Applied ${baseline.ysql.schemaFile} with existing-object warnings`
    });

    for (const statement of baseline.ycql.truncateStatements) {
      runOrThrow(ycqlCommand(databaseMode, ['-e', statement]), `Unable to execute YCQL reset statement: ${statement}`);
    }
    steps.push({
      name: 'truncate-ycql-tables',
      ok: true,
      detail: `Executed ${baseline.ycql.truncateStatements.length} YCQL reset statements`
    });

    for (const statement of baseline.ysql.resetStatements) {
      runOrThrow(ysqlCommand(databaseMode, servicesConfig, ['-c', statement]), `Unable to execute YSQL reset statement: ${statement}`);
    }
    steps.push({
      name: 'truncate-ysql-tables',
      ok: true,
      detail: `Executed ${baseline.ysql.resetStatements.length} YSQL reset statements`
    });

    for (const seed of baseline.ycql.seedData) {
      const sourcePath = databaseMode.kind === 'docker'
        ? (() => {
            const copyResult = copySeedFileToDocker(databaseMode, seed.file);
            runOrThrow(copyResult.result, `Unable to copy ${seed.file} into Docker container ${databaseMode.container}`);
            return copyResult.target;
          })()
        : path.join(repoRoot, seed.file);

      runOrThrow(runSeedCommand(databaseMode, copyStatement(seed, sourcePath)), `Unable to load ${seed.file} into ${seed.table}`);
      steps.push({
        name: `seed-${seed.name}`,
        ok: true,
        detail: `Loaded ${countCsvRows(seed.file)} rows from ${seed.file}`
      });
    }

    for (const verification of baseline.ycql.verificationQueries) {
      const result = ycqlCommand(databaseMode, ['-e', verification.query]);
      const actualCount = parseCount(result.stdout);
      const expectedCount = countCsvRows(verification.sourceFile);
      const ok = result.status === 0 && actualCount === expectedCount;
      steps.push({
        name: `verify-${verification.name}`,
        ok,
        detail: ok
          ? `${verification.name} contains ${actualCount} rows`
          : result.status !== 0
            ? `${verification.name} verification query failed: ${(result.stderr || result.stdout || 'unknown YCQL error').trim()}`
            : `${verification.name} verification failed${actualCount === null ? '' : `: expected ${expectedCount}, found ${actualCount}`}`
      });
    }

    for (const verification of baseline.ysql.verificationQueries) {
      const result = ysqlCommand(databaseMode, servicesConfig, ['-tAc', verification.query]);
      const actualCount = parseCount(result.stdout);
      const ok = result.status === 0 && actualCount === verification.expectedExactRows;
      steps.push({
        name: `verify-${verification.name}`,
        ok,
        detail: ok
          ? `${verification.name} contains ${actualCount} rows`
          : result.status !== 0
            ? `${verification.name} verification query failed: ${(result.stderr || result.stdout || 'unknown YSQL error').trim()}`
            : `${verification.name} verification failed${actualCount === null ? '' : `: expected ${verification.expectedExactRows}, found ${actualCount}`}`
      });
    }
  } catch (error) {
    steps.push({
      name: 'reset-error',
      ok: false,
      detail: error.message
    });
  }

  const ok = steps.every((step) => step.ok);
  const summary = {
    scope,
    ok,
    command: 'reset',
    summaryName,
    steps,
    notes
  };
  writeRunSummary({
    ...runContext,
    summaryJson: path.join(runContext.runDir, `${summaryName}-summary.json`),
    summaryText: path.join(runContext.runDir, `${summaryName}-summary.txt`)
  }, 'Yugastore E2E Reset', summary);
  return summary;
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const summary = await runReset({
    scope: args.scope || 'adhoc'
  });

  if (!summary.ok) {
    process.exitCode = 1;
  }
}

if (require.main === module) {
  main().catch((error) => {
    console.error(error.stack || error.message);
    process.exitCode = 1;
  });
}

module.exports = {
  runReset
};