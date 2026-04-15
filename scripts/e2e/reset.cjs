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

const ycqlDropStatements = [
  'DROP INDEX IF EXISTS cronos.top_products_in_category;',
  'DROP TABLE IF EXISTS cronos.product_rankings;',
  'DROP TABLE IF EXISTS cronos.product_inventory;',
  'DROP TABLE IF EXISTS cronos.orders;',
  'DROP TABLE IF EXISTS cronos.products;'
];

const localSeedColumnConfigs = {
  products: {
    columns: ['asin', 'title', 'description', 'price', 'imurl', 'brand', 'num_reviews', 'num_stars', 'avg_stars'],
    sourceIndexes: [0, 1, 2, 3, 4, 9, 11, 12, 13],
    types: ['text', 'text', 'text', 'double', 'text', 'text', 'int', 'double', 'double']
  },
  product_rankings: {
    columns: ['asin', 'category', 'sales_rank', 'title', 'price', 'imurl', 'num_reviews', 'num_stars', 'avg_stars'],
    sourceIndexes: [0, 1, 2, 3, 4, 5, 6, 7, 8],
    types: ['text', 'text', 'int', 'text', 'double', 'text', 'int', 'double', 'double']
  },
  product_inventory: {
    columns: ['asin', 'quantity'],
    sourceIndexes: [0, 1],
    types: ['text', 'int']
  }
};

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

function splitCollection(value) {
  if (!value) {
    return [];
  }

  const trimmed = value.trim();
  if (trimmed.length < 2) {
    return [];
  }

  const inner = trimmed.slice(1, -1).trim();
  if (!inner) {
    return [];
  }

  return inner.split(',').map((item) => item.trim()).filter(Boolean);
}

function escapeCqlString(value) {
  return `'${String(value).replace(/'/g, "''")}'`;
}

function toCqlLiteral(value, type) {
  if (type === 'text') {
    return escapeCqlString(value || '');
  }

  if (type === 'int' || type === 'double') {
    return value === '' ? '0' : value;
  }

  if (type === 'list<text>') {
    if (!value) {
      return 'null';
    }
    const items = splitCollection(value).map(escapeCqlString);
    return `[${items.join(', ')}]`;
  }

  if (type === 'set<text>') {
    if (!value) {
      return 'null';
    }
    const items = splitCollection(value).map(escapeCqlString);
    return `{${items.join(', ')}}`;
  }

  return 'null';
}

function buildInsertStatements(seed) {
  const sourcePath = path.join(repoRoot, seed.file);
  const lines = fs.readFileSync(sourcePath, 'utf8').split(/\r?\n/).filter(Boolean);
  const config = localSeedColumnConfigs[seed.table];

  if (!config || config.columns.length !== config.types.length || config.columns.length !== config.sourceIndexes.length) {
    throw new Error(`No local seed type mapping is defined for ${seed.table}`);
  }

  return lines.map((line) => {
    const values = parseCsvLine(line);
    const maxSourceIndex = Math.max(...config.sourceIndexes);
    if (values.length <= maxSourceIndex) {
      throw new Error(`Unable to parse ${seed.file}: expected at least ${maxSourceIndex + 1} columns for ${seed.table}, found ${values.length}`);
    }

    const cqlValues = config.sourceIndexes.map((sourceIndex, index) => toCqlLiteral(values[sourceIndex], config.types[index]));
    return `INSERT INTO ${seed.table} (${config.columns.join(', ')}) VALUES (${cqlValues.join(', ')});`;
  });
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

function runLocalInsertFallback(databaseMode, seed) {
  const tempDirectory = fs.mkdtempSync(path.join(os.tmpdir(), 'yugastore-e2e-'));
  const tempFile = path.join(tempDirectory, `${seed.name}-insert.cql`);

  try {
    fs.writeFileSync(tempFile, `${buildInsertStatements(seed).join('\n')}\n`);
    return ycqlCommand(databaseMode, ['-f', tempFile]);
  } finally {
    fs.rmSync(tempDirectory, { recursive: true, force: true });
  }
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

    for (const statement of ycqlDropStatements) {
      runOrThrow(ycqlCommand(databaseMode, ['-e', statement]), `Unable to execute YCQL drop statement: ${statement}`);
    }
    steps.push({
      name: 'drop-ycql-objects',
      ok: true,
      detail: `Dropped ${ycqlDropStatements.length} YCQL objects before schema reapply`
    });

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

      const seedResult = runSeedCommand(databaseMode, copyStatement(seed, sourcePath));
      if (seedResult.status !== 0 && databaseMode.kind === 'local-cli') {
        const fallbackResult = runLocalInsertFallback(databaseMode, seed);
        runOrThrow(fallbackResult, `Unable to load ${seed.file} into ${seed.table}`);
      } else {
        runOrThrow(seedResult, `Unable to load ${seed.file} into ${seed.table}`);
      }
      steps.push({
        name: `seed-${seed.name}`,
        ok: true,
        detail: `Loaded ${countCsvRows(seed.file)} rows from ${seed.file}`
      });
    }

    for (const verification of baseline.ycql.verificationQueries) {
      const result = ycqlCommand(databaseMode, ['-e', verification.query]);
      const actualCount = parseCount(result.stdout);
      const matchingSeed = baseline.ycql.seedData.find((seed) => seed.file === verification.sourceFile);
      const expectedCount = matchingSeed ? countExpectedSeedRows(matchingSeed) : countCsvRows(verification.sourceFile);
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