const { test } = require('@playwright/test');
const { AppShellPage } = require('../page-objects/app-shell.page');
const { AuthPage } = require('../page-objects/auth.page');
const { ProductDetailsPage } = require('../page-objects/product-details.page');
const { StorefrontPage } = require('../page-objects/storefront.page');
const { buildUser } = require('../page-objects/test-data');

test.describe('Seeded product details coverage', () => {
  test('authenticated shopper can open a product details page and add the item to the cart', async ({ page }) => {
    const shell = new AppShellPage(page);
    const authPage = new AuthPage(page);
    const storefront = new StorefrontPage(page);
    const productDetails = new ProductDetailsPage(page);
    const user = buildUser('details');

    await authPage.gotoRegister();
    await authPage.register(user);
    await shell.expectAuthenticated(user.email);

    await storefront.openCategory('Books');
    await storefront.expectProductCards(1);
    const firstTitle = await storefront.firstVisibleProductTitle();
    await storefront.openFirstProduct();

    await productDetails.expectLoaded();
    await productDetails.expectTitle(firstTitle);
    await productDetails.expectPriceVisible();
    await productDetails.expectDescriptionVisible();

    await productDetails.addToCart();
    await shell.expectCartCount(1);
  });
});