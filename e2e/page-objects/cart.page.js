const { expect } = require('@playwright/test');

class CartPage {
  constructor(page) {
    this.page = page;
  }

  async expectLoaded() {
    await expect(this.page.locator('.cart-container')).toBeVisible();
    await expect(this.page.getByRole('heading', { name: /Items in cart|Thank you!/ })).toBeVisible();
  }

  async expectProduct(title) {
    await expect(this.page.locator('.cart-item .details', { hasText: title })).toBeVisible();
  }

  async expectItemCount(minimumCount) {
    const items = this.page.locator('.cart-item');
    await expect(items.first()).toBeVisible();
    const count = await items.count();
    expect(count).toBeGreaterThanOrEqual(minimumCount);
  }

  async checkout() {
    await this.page.getByRole('button', { name: 'Checkout' }).click();
    await expect(this.page.getByRole('heading', { name: 'Thank you!' })).toBeVisible();
  }

  async expectOrderConfirmation() {
    await expect(this.page.locator('.cart-container')).toContainText('Your order');
    await expect(this.page.locator('.order-details')).toBeVisible();
  }

  async reload() {
    await this.page.reload();
    await this.expectLoaded();
  }

  async expectEmptyCart() {
    await expect(this.page.locator('.cart-container')).toContainText('Cart is empty');
  }
}

module.exports = {
  CartPage
};