const { test, expect } = require('@playwright/test');
const { AppShellPage } = require('../page-objects/app-shell.page');
const { AuthPage } = require('../page-objects/auth.page');
const { CartPage } = require('../page-objects/cart.page');
const { StorefrontPage } = require('../page-objects/storefront.page');
const { buildUser } = require('../page-objects/test-data');

test.describe('Cart and checkout regression coverage', () => {
  test('authenticated shopper can purchase a seeded storefront product', async ({ page }) => {
    const shell = new AppShellPage(page);
    const authPage = new AuthPage(page);
    const storefront = new StorefrontPage(page);
    const cartPage = new CartPage(page);
    const user = buildUser('shopper');

    await authPage.gotoRegister();
    await authPage.register(user);
    await shell.expectAuthenticated(user.email);

    await storefront.openCategory('Books');
    await storefront.expectProductCards(1);
    const firstTitle = await storefront.addFirstProductToCart();
    expect(firstTitle).not.toEqual('');
    await shell.expectCartCount(1);

    await shell.openCart();
    await cartPage.expectLoaded();
    await cartPage.expectItemCount(1);
    await cartPage.expectProduct(firstTitle);

    await cartPage.checkout();
    await expect(page).toHaveURL(/\/cart$/);
    await cartPage.expectOrderConfirmation();
    await shell.expectCartCount(0);

    await cartPage.reload();
    await cartPage.expectEmptyCart();
  });
});