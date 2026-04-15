const { expect } = require('@playwright/test');

class AppShellPage {
  constructor(page) {
    this.page = page;
  }

  async goto(pathname = '/') {
    await this.page.goto(pathname);
    await expect(this.page.locator('.nav-bar')).toBeVisible();
  }

  async expectGuestState() {
    await expect(this.page.getByRole('link', { name: 'Sign In', exact: true })).toBeVisible();
    await expect(this.page.getByRole('link', { name: 'Register', exact: true })).toBeVisible();
    await expect(this.page.locator('.nav-auth-user')).toHaveCount(0);
  }

  async expectAuthenticated(email) {
    await expect(this.page.getByRole('link', { name: 'Merchant Setup' })).toBeVisible();
    await expect(this.page.locator('.nav-auth-user')).toHaveText(email);
    await expect(this.page.getByRole('button', { name: 'Logout' })).toBeVisible();
  }

  async logout() {
    await this.page.getByRole('button', { name: 'Logout' }).click();
    await this.expectGuestState();
  }

  async expectCartCount(total) {
    const cartCount = this.page.locator('.nav-cart-count');
    if (total > 0) {
      await expect(cartCount).toHaveText(String(total));
      return;
    }

    await expect(cartCount).toHaveCount(0);
  }

  async openCart() {
    await this.page.locator('.nav-cart a').click();
    await expect(this.page).toHaveURL(/\/cart$/);
  }
}

module.exports = {
  AppShellPage
};