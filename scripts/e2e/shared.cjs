const fs = require('fs');
const net = require('net');
const os = require('os');
const path = require('path');
const { spawnSync } = require('child_process');

const repoRoot = path.resolve(__dirname, '..', '..');
const e2eRoot = path.join(repoRoot, 'e2e');
const defaultArtifactRoot = path.join(repoRoot, '.tmp', 'e2e');

function parseArgs(argv) {
  const args = { _: [] };

  for (let index = 0; index < argv.length; index += 1) {
    const current = argv[index];
    if (!current.startsWith('--')) {
      args._.push(current);
      continue;
    }

    const [, keyValue] = current.split('--');
    const [key, inlineValue] = keyValue.split('=');
    if (inlineValue !== undefined) {
      args[key] = inlineValue;
      continue;
    }

    const next = argv[index + 1];
    if (next && !next.startsWith('--')) {
      args[key] = next;
      index += 1;
      continue;
    }

    args[key] = true;
  }

  return args;
}

function ensureDir(dirPath) {
  fs.mkdirSync(dirPath, { recursive: true });
  return dirPath;
}

function loadJson(relativePath) {
  return JSON.parse(fs.readFileSync(path.join(repoRoot, relativePath), 'utf8'));
}

function writeJson(filePath, content) {
  ensureDir(path.dirname(filePath));
  fs.writeFileSync(filePath, `${JSON.stringify(content, null, 2)}${os.EOL}`);
}

function writeText(filePath, content) {
  ensureDir(path.dirname(filePath));
  fs.writeFileSync(filePath, `${content}${os.EOL}`);
}

function timestamp() {
  return new Date().toISOString().replace(/[:.]/g, '-');
}

function createRunContext(scope) {
  const runId = `${scope}-${timestamp()}`;
  const runDir = path.join(defaultArtifactRoot, scope, runId);
  const playwrightDir = path.join(runDir, 'playwright');
  ensureDir(playwrightDir);
  return {
    scope,
    runId,
    runDir,
    playwrightDir,
    summaryJson: path.join(runDir, 'summary.json'),
    summaryText: path.join(runDir, 'summary.txt')
  };
}

function findExecutable(name, extraCandidates = []) {
  for (const candidate of extraCandidates) {
    if (candidate && fs.existsSync(candidate)) {
      return candidate;
    }
  }

  const pathEntries = (process.env.PATH || '').split(path.delimiter).filter(Boolean);
  for (const entry of pathEntries) {
    const candidate = path.join(entry, name);
    if (fs.existsSync(candidate)) {
      return candidate;
    }
  }

  return null;
}

function runCommand(command, args, options = {}) {
  return spawnSync(command, args, {
    cwd: options.cwd || repoRoot,
    env: { ...process.env, ...(options.env || {}) },
    encoding: 'utf8',
    input: options.input,
    shell: false,
    stdio: options.stdio || 'pipe'
  });
}

function runShell(command, options = {}) {
  return spawnSync('/bin/sh', ['-lc', command], {
    cwd: options.cwd || repoRoot,
    env: { ...process.env, ...(options.env || {}) },
    encoding: 'utf8',
    input: options.input
  });
}

function parseMajorVersion(versionText) {
  const match = versionText.match(/(\d+)(?:\.\d+)?(?:\.\d+)?/);
  return match ? Number(match[1]) : null;
}

function parseCount(output) {
  const lines = output
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean);

  for (const line of lines) {
    if (/^\d+$/.test(line)) {
      return Number(line);
    }
  }

  for (const line of lines) {
    const pipeMatch = line.match(/^\|?\s*(\d+)\s*\|?$/);
    if (pipeMatch) {
      return Number(pipeMatch[1]);
    }
  }

  const matches = output.match(/\b\d+\b/g);
  if (!matches || matches.length === 0) {
    return null;
  }
  return Number(matches[0]);
}

function countCsvRows(relativePath) {
  const absolutePath = path.join(repoRoot, relativePath);
  const contents = fs.readFileSync(absolutePath, 'utf8').trimEnd();
  if (!contents) {
    return 0;
  }
  return contents.split(/\r?\n/).length;
}

function formatStep(step) {
  const marker = step.ok ? 'PASS' : 'FAIL';
  return `${marker}: ${step.name}${step.detail ? ` - ${step.detail}` : ''}`;
}

function writeRunSummary(runContext, title, summary) {
  const lines = [
    title,
    `Scope: ${summary.scope}`,
    `Run ID: ${runContext.runId}`,
    `Status: ${summary.ok ? 'PASS' : 'FAIL'}`,
    `Artifacts: ${runContext.runDir}`,
    ''
  ];

  for (const step of summary.steps) {
    lines.push(formatStep(step));
  }

  if (summary.notes && summary.notes.length > 0) {
    lines.push('', 'Notes:');
    for (const note of summary.notes) {
      lines.push(`- ${note}`);
    }
  }

  writeJson(runContext.summaryJson, summary);
  writeText(runContext.summaryText, lines.join(os.EOL));
}

function probeTcp(host, port, timeoutMs = 1500) {
  return new Promise((resolve) => {
    const socket = new net.Socket();
    let resolved = false;

    const finish = (ok, detail) => {
      if (resolved) {
        return;
      }
      resolved = true;
      socket.destroy();
      resolve({ ok, detail });
    };

    socket.setTimeout(timeoutMs);
    socket.once('connect', () => finish(true, `${host}:${port} accepted a TCP connection`));
    socket.once('timeout', () => finish(false, `${host}:${port} timed out`));
    socket.once('error', (error) => finish(false, `${host}:${port} rejected the connection (${error.code || error.message})`));
    socket.connect(port, host);
  });
}

function detectDatabaseMode(servicesConfig) {
  const dockerExecutable = findExecutable('docker');
  if (dockerExecutable) {
    const result = runCommand(dockerExecutable, ['ps', '--format', '{{.Names}}']);
    if (result.status === 0) {
      const containerNames = result.stdout.split(/\r?\n/).map((entry) => entry.trim()).filter(Boolean);
      if (containerNames.includes(servicesConfig.database.dockerContainer)) {
        return {
          kind: 'docker',
          dockerExecutable,
          container: servicesConfig.database.dockerContainer
        };
      }
    }
  }

  const ycqlHelper = path.join(repoRoot, 'ycqlsh.sh');
  const ycqlExecutable = fs.existsSync(ycqlHelper)
    ? ycqlHelper
    : findExecutable('ycqlsh');
  const ysqlExecutable = findExecutable('ysqlsh', [
    process.env.YB_HOME ? path.join(process.env.YB_HOME, 'bin', 'ysqlsh') : null,
    process.env.YUGABYTE_HOME ? path.join(process.env.YUGABYTE_HOME, 'bin', 'ysqlsh') : null,
    path.join(os.homedir(), 'Downloads', 'dev-runtime', 'yugabyte-2025.2.2.2', 'bin', 'ysqlsh'),
    path.join(os.homedir(), 'yugabyte', 'bin', 'ysqlsh')
  ]);

  if (ycqlExecutable && ysqlExecutable) {
    return {
      kind: 'local-cli',
      ycqlExecutable,
      ysqlExecutable
    };
  }

  return null;
}

function ycqlCommand(mode, args, options = {}) {
  if (mode.kind === 'docker') {
    return runCommand(mode.dockerExecutable, ['exec', '-i', mode.container, 'ycqlsh', '127.0.0.1', '9042', '-k', 'cronos', ...args], options);
  }
  return runCommand(mode.ycqlExecutable, args, options);
}

function ysqlCommand(mode, servicesConfig, args, options = {}) {
  if (mode.kind === 'docker') {
    return runCommand(mode.dockerExecutable, ['exec', '-i', mode.container, 'ysqlsh', '-h', '127.0.0.1', '-p', String(servicesConfig.database.ysql.port), ...args], options);
  }
  return runCommand(mode.ysqlExecutable, ['-h', servicesConfig.database.ysql.host, '-p', String(servicesConfig.database.ysql.port), ...args], options);
}

function copySeedFileToDocker(mode, relativePath) {
  const source = path.join(repoRoot, relativePath);
  const target = `/tmp/${path.basename(relativePath)}`;
  const result = runCommand(mode.dockerExecutable, ['cp', source, `${mode.container}:${target}`]);
  return { result, target };
}

module.exports = {
  countCsvRows,
  createRunContext,
  defaultArtifactRoot,
  detectDatabaseMode,
  e2eRoot,
  ensureDir,
  findExecutable,
  formatStep,
  loadJson,
  parseArgs,
  parseCount,
  parseMajorVersion,
  probeTcp,
  repoRoot,
  runCommand,
  runShell,
  writeJson,
  writeRunSummary,
  ycqlCommand,
  ysqlCommand,
  copySeedFileToDocker
};