const { test } = require('@playwright/test');
const { AppShellPage } = require('../page-objects/app-shell.page');
const { AuthPage } = require('../page-objects/auth.page');
const { CartPage } = require('../page-objects/cart.page');
const { MerchantSignupPage } = require('../page-objects/merchant-signup.page');
const { StorefrontPage } = require('../page-objects/storefront.page');
const { buildUser } = require('../page-objects/test-data');

test.describe('Auth session restore and guarded routes', () => {
  test('guest users are prompted to sign in on protected cart and merchant routes', async ({ page }) => {
    const shell = new AppShellPage(page);
    const cartPage = new CartPage(page);
    const merchantSignupPage = new MerchantSignupPage(page);

    await shell.goto('/');
    await shell.expectGuestState();
    await shell.openCart();
    await cartPage.expectSignInRequired();

    await merchantSignupPage.goto();
    await shell.expectGuestState();
    await merchantSignupPage.expectSignInRequired();
  });

  test('authenticated session and cart state survive a browser reload', async ({ page }) => {
    const shell = new AppShellPage(page);
    const authPage = new AuthPage(page);
    const storefront = new StorefrontPage(page);
    const user = buildUser('restore');

    await authPage.gotoRegister();
    await authPage.register(user);
    await shell.expectAuthenticated(user.email);

    await storefront.openCategory('Books');
    await storefront.expectProductCards(1);
    await storefront.addFirstProductToCart();
    await shell.expectCartCount(1);

    await page.reload();
    await shell.expectAuthenticated(user.email);
    await shell.expectCartCount(1);
  });
});