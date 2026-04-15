function uniqueSuffix(prefix) {
  const randomPart = Math.random().toString(36).slice(2, 8);
  return `${prefix}-${Date.now()}-${randomPart}`;
}

function buildUser(prefix) {
  const suffix = uniqueSuffix(prefix);
  return {
    email: `${suffix}@example.test`,
    password: 'Password123!'
  };
}

function buildMerchant(prefix) {
  const suffix = uniqueSuffix(prefix);
  return {
    companyName: `Merchant ${suffix}`,
    tenantKey: `tenant-${suffix}`
  };
}

module.exports = {
  buildMerchant,
  buildUser
};