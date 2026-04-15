const { test, expect } = require('@playwright/test');
const { AppShellPage } = require('../page-objects/app-shell.page');
const { StorefrontPage } = require('../page-objects/storefront.page');

test.describe('Critical smoke availability and storefront coverage', () => {
  test('required entrypoints are reachable @smoke', async ({ request, baseURL }) => {
    const uiResponse = await request.get(baseURL || 'http://localhost:8080');
    expect(uiResponse.ok()).toBeTruthy();
    await expect(await uiResponse.text()).toContain('<div id="root"></div>');

    const eurekaResponse = await request.get('http://localhost:8761/');
    expect(eurekaResponse.ok()).toBeTruthy();
    await expect(await eurekaResponse.text()).toContain('<title>Eureka</title>');

    const gatewayCatalogResponse = await request.get('http://localhost:8081/api/v1/products?limit=2&offset=0');
    expect(gatewayCatalogResponse.ok()).toBeTruthy();
    const products = await gatewayCatalogResponse.json();
    expect(Array.isArray(products)).toBeTruthy();
    expect(products.length).toBeGreaterThan(0);
  });

  test('seeded storefront catalog is visible @smoke', async ({ page }) => {
    const shell = new AppShellPage(page);
    const storefront = new StorefrontPage(page);

    await storefront.goto();
    await shell.expectGuestState();
    await storefront.openCategory('Books');
    await storefront.expectProductCards(1);
    const firstTitle = await storefront.firstVisibleProductTitle();
    expect(firstTitle).not.toEqual('');
    await expect(page.locator('.highlights-title')).toContainText('Books');
    await expect(page.locator('.products')).toContainText(firstTitle);
    await expect(page.locator('.products .price-add').first()).toContainText('$');
  });
});