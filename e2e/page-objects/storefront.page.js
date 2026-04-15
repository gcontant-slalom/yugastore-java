const { expect } = require('@playwright/test');

class StorefrontPage {
  constructor(page) {
    this.page = page;
  }

  async goto() {
    await this.page.goto('/');
    await expect(this.page.locator('.hero')).toBeVisible();
  }

  async openCategory(category) {
    await this.page.getByRole('link', { name: category }).first().click();
    await expect(this.page).toHaveURL(new RegExp(`/${category.replace(/[-/\\^$*+?.()|[\]{}]/g, '\\$&')}$`));
    await expect(this.page.locator('.highlights-title')).toContainText(category);
  }

  async expectProductCards(minimumCount) {
    const items = this.page.locator('.products .item');
    await expect(items.first()).toBeVisible();
    const count = await items.count();
    expect(count).toBeGreaterThanOrEqual(minimumCount);
  }

  async expectProductTitle(title) {
    await expect(this.page.locator('.product-name', { hasText: title }).first()).toBeVisible();
  }

  async firstVisibleProductTitle() {
    const title = await this.page.locator('.products .product-name').first().textContent();
    return (title || '').trim();
  }

  async openProduct(title) {
    await this.page.locator('.product-name', { hasText: title }).first().click();
  }

  async openFirstProduct() {
    await this.page.locator('.products .product-name').first().click();
  }

  async addProductToCart(title) {
    const productCard = this.page.locator('.item').filter({
      has: this.page.locator('.product-name', { hasText: title })
    }).first();
    await expect(productCard).toBeVisible();
    await productCard.locator('.price-add').click();
  }

  async addFirstProductToCart() {
    const title = await this.firstVisibleProductTitle();
    await this.page.locator('.products .item').first().locator('.price-add').click();
    return title;
  }
}

module.exports = {
  StorefrontPage
};