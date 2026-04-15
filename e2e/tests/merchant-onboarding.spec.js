const { test } = require('@playwright/test');
const { AppShellPage } = require('../page-objects/app-shell.page');
const { AuthPage } = require('../page-objects/auth.page');
const { MerchantSignupPage } = require('../page-objects/merchant-signup.page');
const { buildMerchant, buildUser } = require('../page-objects/test-data');

test.describe('Merchant onboarding and tenant visibility', () => {
  test('merchant can sign up, create a tenant, logout, and regain tenant visibility', async ({ page }) => {
    const shell = new AppShellPage(page);
    const authPage = new AuthPage(page);
    const merchantSignupPage = new MerchantSignupPage(page);
    const user = buildUser('merchant');
    const merchant = buildMerchant('northwind');

    await authPage.gotoRegister();
    await authPage.register(user);
    await shell.expectAuthenticated(user.email);

    await merchantSignupPage.goto();
    await merchantSignupPage.createTenant(merchant);
    await merchantSignupPage.expectTenantCreated(merchant);
    await merchantSignupPage.expectTenantListed(merchant);

    await shell.logout();

    await authPage.gotoLogin();
    await authPage.login(user);
    await shell.expectAuthenticated(user.email);

    await merchantSignupPage.goto();
    await merchantSignupPage.expectTenantListed(merchant);
  });
});