const { expect } = require('@playwright/test');

class ProductDetailsPage {
  constructor(page) {
    this.page = page;
  }

  async expectLoaded() {
    await expect(this.page.locator('.show-product')).toBeVisible();
    await expect(this.page.locator('#product-name')).toBeVisible();
  }

  async expectTitle(title) {
    await expect(this.page.locator('#product-name')).toHaveText(title);
  }

  async expectPriceVisible() {
    await expect(this.page.locator('.product-price').first()).toContainText('$');
  }

  async expectDescriptionVisible() {
    await expect(this.page.locator('.product-description')).not.toHaveText('');
  }

  async expectRelatedProducts(minimumCount) {
    const relatedItems = this.page.locator('.content-white .products .item');
    await expect(relatedItems.first()).toBeVisible();
    const count = await relatedItems.count();
    expect(count).toBeGreaterThanOrEqual(minimumCount);
  }

  async addToCart() {
    await this.page.getByRole('button', { name: /Add to cart/i }).first().click();
  }
}

module.exports = {
  ProductDetailsPage
};