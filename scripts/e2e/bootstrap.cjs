const fs = require('fs');
const path = require('path');
const {
  countCsvRows,
  createRunContext,
  detectDatabaseMode,
  e2eRoot,
  loadJson,
  parseArgs,
  parseCount,
  parseMajorVersion,
  probeTcp,
  repoRoot,
  runCommand,
  writeRunSummary,
  ycqlCommand,
  ysqlCommand
} = require('./shared.cjs');

const seedPrimaryKeyIndexes = {
  products: [0],
  product_rankings: [0, 1],
  product_inventory: [0]
};

function parseCsvLine(line) {
  const values = [];
  let current = '';
  let inQuotes = false;

  for (let index = 0; index < line.length; index += 1) {
    const char = line[index];
    const nextChar = line[index + 1];

    if (inQuotes && char === '\\' && (nextChar === '"' || nextChar === '\\')) {
      current += nextChar;
      index += 1;
      continue;
    }

    if (char === '"') {
      if (inQuotes && nextChar === '"') {
        current += '"';
        index += 1;
      } else {
        inQuotes = !inQuotes;
      }
      continue;
    }

    if (char === ',' && !inQuotes) {
      values.push(current);
      current = '';
      continue;
    }

    current += char;
  }

  values.push(current);
  return values;
}

function countExpectedSeedRows(seed) {
  const primaryKeyIndexes = seedPrimaryKeyIndexes[seed.table];
  if (!primaryKeyIndexes) {
    return countCsvRows(seed.file);
  }

  const sourcePath = path.join(repoRoot, seed.file);
  const lines = fs.readFileSync(sourcePath, 'utf8').split(/\r?\n/).filter(Boolean);
  const keys = new Set();

  for (const line of lines) {
    const values = parseCsvLine(line);
    keys.add(primaryKeyIndexes.map((index) => values[index] || '').join('\u0000'));
  }

  return keys.size;
}

async function runBootstrap(options = {}) {
  const scope = options.scope || 'adhoc';
  const runContext = options.runContext || createRunContext(scope);
  const summaryName = options.summaryName || 'bootstrap';
  const servicesConfig = loadJson('e2e/config/services.json');
  const baseline = loadJson('e2e/fixtures/baseline.json');
  const steps = [];
  const notes = [];

  const javaVersionResult = runCommand('java', ['-version']);
  const javaVersionText = `${javaVersionResult.stderr || ''}${javaVersionResult.stdout || ''}`;
  const javaMajor = parseMajorVersion(javaVersionText);
  const javaOk = javaVersionResult.status === 0 && javaMajor !== null && javaMajor >= servicesConfig.prerequisites.javaMajorVersion;
  steps.push({
    name: 'java-runtime',
    ok: javaOk,
    detail: javaOk
      ? `Java ${javaMajor} satisfies the minimum required version ${servicesConfig.prerequisites.javaMajorVersion}`
      : `Java ${servicesConfig.prerequisites.javaMajorVersion}+ is required`
  });

  const nodeMajor = parseMajorVersion(process.version);
  const nodeOk = nodeMajor !== null && nodeMajor >= servicesConfig.prerequisites.nodeMajorVersion;
  steps.push({
    name: 'node-runtime',
    ok: nodeOk,
    detail: nodeOk
      ? `Node ${process.version} satisfies the minimum required version ${servicesConfig.prerequisites.nodeMajorVersion}`
      : `Node ${servicesConfig.prerequisites.nodeMajorVersion}+ is required`
  });

  const npmVersionResult = runCommand('npm', ['-v']);
  const npmOk = npmVersionResult.status === 0;
  steps.push({
    name: 'npm-runtime',
    ok: npmOk,
    detail: npmOk ? `npm ${npmVersionResult.stdout.trim()} is available` : 'npm is required to install the e2e workspace dependencies'
  });

  const playwrightPackagePath = path.join(e2eRoot, 'node_modules', '@playwright', 'test', 'package.json');
  const playwrightOk = fs.existsSync(playwrightPackagePath);
  steps.push({
    name: 'playwright-dependency',
    ok: playwrightOk,
    detail: playwrightOk
      ? '@playwright/test is installed in e2e/node_modules'
      : 'Install e2e dependencies with `cd e2e && npm install --package-lock=false` before running the harness'
  });

  for (const service of servicesConfig.requiredServices) {
    const probe = await probeTcp(service.host, service.port);
    steps.push({
      name: `service-${service.id}`,
      ok: probe.ok,
      detail: probe.ok ? `${service.description} is reachable at ${service.url}` : `${service.description} is not reachable at ${service.url}`
    });
  }

  for (const service of servicesConfig.optionalServices) {
    const probe = await probeTcp(service.host, service.port);
    steps.push({
      name: `optional-service-${service.id}`,
      ok: true,
      detail: probe.ok
        ? `${service.description} is reachable at ${service.url}`
        : `${service.description} is not running; this is expected for the current supported baseline`
    });
  }

  if (!options.skipDataChecks) {
    const databaseMode = detectDatabaseMode(servicesConfig);
    if (!databaseMode) {
      steps.push({
        name: 'database-mode',
        ok: false,
        detail: 'Unable to find a running `yugastore-yb` container or a local ycqlsh/ysqlsh pair for baseline validation'
      });
      notes.push('Start the YugabyteDB Docker container named `yugastore-yb`, or install both `ycqlsh` and `ysqlsh` locally before rerunning bootstrap.');
    } else {
      steps.push({
        name: 'database-mode',
        ok: true,
        detail: databaseMode.kind === 'docker' ? `Using Docker container ${databaseMode.container}` : 'Using local ycqlsh/ysqlsh commands'
      });

      for (const verification of baseline.ycql.verificationQueries) {
        const result = ycqlCommand(databaseMode, ['-e', verification.query]);
        const actualCount = result.status === 0 ? parseCount(result.stdout) : null;
        const matchingSeed = baseline.ycql.seedData.find((seed) => seed.file === verification.sourceFile);
        const expectedCount = matchingSeed ? countExpectedSeedRows(matchingSeed) : countCsvRows(verification.sourceFile);
        const ok = result.status === 0 && actualCount === expectedCount;
        steps.push({
          name: `ycql-${verification.name}`,
          ok,
          detail: ok
            ? `${verification.name} contains ${actualCount} rows, matching ${verification.sourceFile}`
            : result.status !== 0
              ? `${verification.name} baseline query failed: ${(result.stderr || result.stdout || 'unknown YCQL error').trim()}`
              : `${verification.name} baseline mismatch${actualCount === null ? '' : `: expected ${expectedCount}, found ${actualCount}`}`
        });
      }

      for (const verification of baseline.ysql.verificationQueries) {
        const result = ysqlCommand(databaseMode, servicesConfig, ['-tAc', verification.query]);
        const actualCount = result.status === 0 ? parseCount(result.stdout) : null;
        const ok = result.status === 0 && actualCount === verification.expectedExactRows;
        steps.push({
          name: `ysql-${verification.name}`,
          ok,
          detail: ok
            ? `${verification.name} contains ${actualCount} rows as expected`
            : result.status !== 0
              ? `${verification.name} baseline query failed: ${(result.stderr || result.stdout || 'unknown YSQL error').trim()}`
              : `${verification.name} baseline mismatch${actualCount === null ? '' : `: expected ${verification.expectedExactRows}, found ${actualCount}`}`
        });
      }
    }
  } else {
    notes.push('Data validation was skipped for the pre-reset bootstrap phase.');
  }

  const ok = steps.every((step) => step.ok);
  const summary = {
    scope,
    ok,
    command: 'bootstrap',
    summaryName,
    skipDataChecks: Boolean(options.skipDataChecks),
    steps,
    notes
  };

  writeRunSummary({
    ...runContext,
    summaryJson: path.join(runContext.runDir, `${summaryName}-summary.json`),
    summaryText: path.join(runContext.runDir, `${summaryName}-summary.txt`)
  }, 'Yugastore E2E Bootstrap', summary);
  return summary;
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const summary = await runBootstrap({
    scope: args.scope || 'adhoc',
    skipDataChecks: Boolean(args['skip-data-checks'])
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
  runBootstrap
};