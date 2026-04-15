const { expect } = require('@playwright/test');

class AuthPage {
  constructor(page) {
    this.page = page;
  }

  async gotoRegister() {
    await this.page.goto('/register');
    await expect(this.page.getByRole('heading', { name: 'Create your account' })).toBeVisible();
  }

  async gotoLogin() {
    await this.page.goto('/login');
    await expect(this.page.getByRole('heading', { name: 'Sign in to continue' })).toBeVisible();
  }

  async register(credentials) {
    await this.page.locator('input[name="email"]').fill(credentials.email);
    await this.page.locator('input[name="password"]').fill(credentials.password);
    await this.page.locator('input[name="passwordConfirm"]').fill(credentials.password);
    await this.page.getByRole('button', { name: 'Create Account' }).click();
  }

  async login(credentials) {
    await this.page.locator('input[name="email"]').fill(credentials.email);
    await this.page.locator('input[name="password"]').fill(credentials.password);
    await this.page.getByRole('button', { name: 'Sign In' }).click();
  }
}

module.exports = {
  AuthPage
};