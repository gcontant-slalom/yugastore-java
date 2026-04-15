const { expect } = require('@playwright/test');

class MerchantSignupPage {
  constructor(page) {
    this.page = page;
  }

  async goto() {
    await this.page.goto('/merchant/signup');
    await expect(this.page.getByRole('heading', { name: 'Create your merchant tenant' })).toBeVisible();
  }

  async expectSignInRequired() {
    await expect(this.page.locator('.auth-message')).toContainText('Sign in first, then return to this onboarding route.');
    await expect(this.page.getByRole('link', { name: 'Go to sign in' })).toBeVisible();
    await expect(this.page.getByRole('button', { name: 'Create merchant tenant' })).toBeDisabled();
  }

  async createTenant(merchant) {
    await this.page.locator('input[name="companyName"]').fill(merchant.companyName);
    await this.page.locator('input[name="tenantKey"]').fill(merchant.tenantKey);
    await this.page.getByRole('button', { name: 'Create merchant tenant' }).click();
  }

  async expectTenantCreated(merchant) {
    await expect(this.page.getByRole('heading', { name: 'Merchant tenant created' })).toBeVisible();
    await expect(this.page.locator('.merchant-signup-success')).toContainText(merchant.companyName);
    await expect(this.page.locator('.merchant-signup-success')).toContainText(merchant.tenantKey);
  }

  async expectTenantListed(merchant) {
    const tenantList = this.page.locator('.merchant-tenant-list');
    await expect(tenantList).toContainText(merchant.companyName);
    await expect(tenantList).toContainText(merchant.tenantKey);
  }
}

module.exports = {
  MerchantSignupPage
};