const path = require('path');
const fs = require('fs');
const { runBootstrap } = require('./bootstrap.cjs');
const { runReset } = require('./reset.cjs');
const {
  createRunContext,
  e2eRoot,
  loadJson,
  parseArgs,
  runCommand,
  writeRunSummary
} = require('./shared.cjs');

function printSummary(filePath, stream = process.stdout) {
  if (!fs.existsSync(filePath)) {
    return;
  }

  const contents = fs.readFileSync(filePath, 'utf8').trim();
  if (!contents) {
    return;
  }

  stream.write(`${contents}\n`);
}

async function runScope(scope) {
  const scopes = loadJson('e2e/config/scopes.json');
  const scopeConfig = scopes[scope];
  if (!scopeConfig) {
    throw new Error(`Unsupported regression scope: ${scope}`);
  }

  const runContext = createRunContext(scope);
  const bootstrapPreReset = await runBootstrap({
    scope,
    runContext,
    summaryName: 'bootstrap-pre-reset',
    skipDataChecks: true
  });

  if (!bootstrapPreReset.ok) {
    const summaryText = path.join(runContext.runDir, 'bootstrap-pre-reset-summary.txt');
    writeRunSummary(runContext, 'Yugastore E2E Scope Runner', {
      scope,
      ok: false,
      command: scope,
      steps: [
        {
          name: 'pre-reset-bootstrap',
          ok: false,
          detail: 'Runtime or service prerequisites failed before reset'
        }
      ],
      notes: [
        `See ${path.join(runContext.runDir, 'bootstrap-pre-reset-summary.json')} for the detailed bootstrap output.`
      ]
    });
    printSummary(summaryText, process.stderr);
    return false;
  }

  const resetSummary = await runReset({
    scope,
    runContext,
    summaryName: 'reset'
  });
  if (!resetSummary.ok) {
    const summaryText = path.join(runContext.runDir, 'reset-summary.txt');
    writeRunSummary(runContext, 'Yugastore E2E Scope Runner', {
      scope,
      ok: false,
      command: scope,
      steps: [
        {
          name: 'reset',
          ok: false,
          detail: 'Fixture reset failed before Playwright execution'
        }
      ],
      notes: [
        `See ${path.join(runContext.runDir, 'reset-summary.json')} for the detailed reset output.`
      ]
    });
    printSummary(summaryText, process.stderr);
    return false;
  }

  const bootstrapPostReset = await runBootstrap({
    scope,
    runContext,
    summaryName: 'bootstrap-post-reset',
    skipDataChecks: false
  });
  if (!bootstrapPostReset.ok) {
    const summaryText = path.join(runContext.runDir, 'bootstrap-post-reset-summary.txt');
    writeRunSummary(runContext, 'Yugastore E2E Scope Runner', {
      scope,
      ok: false,
      command: scope,
      steps: [
        {
          name: 'post-reset-bootstrap',
          ok: false,
          detail: 'The reset completed but the baseline validation still failed'
        }
      ],
      notes: [
        `See ${path.join(runContext.runDir, 'bootstrap-post-reset-summary.json')} for the detailed bootstrap output.`
      ]
    });
    printSummary(summaryText, process.stderr);
    return false;
  }

  const playwrightArgs = [
    '--no-install',
    'playwright',
    'test',
    '--config',
    'playwright.config.cjs',
    ...scopeConfig.playwrightArgs
  ];

  const playwrightResult = runCommand('npx', playwrightArgs, {
    cwd: e2eRoot,
    env: {
      E2E_RUN_ARTIFACT_DIR: runContext.playwrightDir,
      E2E_RUN_PLAYWRIGHT_REPORT_DIR: path.join(runContext.playwrightDir, 'html-report')
    },
    stdio: 'inherit'
  });

  const ok = playwrightResult.status === 0;
  writeRunSummary(runContext, 'Yugastore E2E Scope Runner', {
    scope,
    ok,
    command: scope,
    steps: [
      {
        name: 'pre-reset-bootstrap',
        ok: bootstrapPreReset.ok,
        detail: 'Runtime prerequisites and required services passed before reset'
      },
      {
        name: 'reset',
        ok: resetSummary.ok,
        detail: 'Deterministic baseline reset completed'
      },
      {
        name: 'post-reset-bootstrap',
        ok: bootstrapPostReset.ok,
        detail: 'Seed data and baseline state matched the documented expectations'
      },
      {
        name: 'playwright',
        ok,
        detail: ok
          ? `Scope ${scope} completed successfully`
          : 'Playwright returned a non-zero exit code'
      }
    ],
    notes: [
      scopeConfig.description,
      `Playwright artifacts: ${runContext.playwrightDir}`,
      'Issue #24 intentionally allows zero journey specs; `--pass-with-no-tests` keeps the foundation command surface stable until issue #25 adds coverage.'
    ]
  });

  if (!ok) {
    process.stderr.write('Playwright failed. See the terminal output above for details.');
  } else {
    printSummary(runContext.summaryText);
  }

  return ok;
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const scope = args._[0] || args.scope;
  if (!scope) {
    throw new Error('Usage: node ../scripts/e2e/run-scope.cjs <smoke|full>');
  }

  const ok = await runScope(scope);
  if (!ok) {
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
  runScope
};