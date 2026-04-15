const path = require('path');
const { defineConfig } = require('@playwright/test');

const artifactDir = process.env.E2E_RUN_ARTIFACT_DIR
  ? path.resolve(process.env.E2E_RUN_ARTIFACT_DIR)
  : path.resolve(__dirname, '..', '.tmp', 'e2e', 'adhoc');

const htmlReportDir = process.env.E2E_RUN_PLAYWRIGHT_REPORT_DIR
  ? path.resolve(process.env.E2E_RUN_PLAYWRIGHT_REPORT_DIR)
  : path.join(artifactDir, 'playwright-report');

module.exports = defineConfig({
  testDir: path.join(__dirname, 'tests'),
  fullyParallel: false,
  workers: 1,
  retries: 0,
  timeout: 30_000,
  reporter: [
    ['list'],
    ['json', { outputFile: path.join(artifactDir, 'results.json') }],
    ['html', { outputFolder: htmlReportDir, open: 'never' }]
  ],
  outputDir: path.join(artifactDir, 'test-results'),
  use: {
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:8080',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure'
  }
});