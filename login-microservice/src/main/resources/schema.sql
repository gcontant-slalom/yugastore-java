CREATE TABLE IF NOT EXISTS "user" (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS merchant_tenant (
  id BIGSERIAL PRIMARY KEY,
  display_name VARCHAR(255) NOT NULL,
  tenant_key VARCHAR(255) NOT NULL UNIQUE,
  created_by_user_id BIGINT NOT NULL REFERENCES "user"(id)
);

CREATE TABLE IF NOT EXISTS merchant_membership (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES "user"(id),
  merchant_tenant_id BIGINT NOT NULL REFERENCES merchant_tenant(id),
  role_name VARCHAR(64) NOT NULL,
  CONSTRAINT merchant_membership_user_tenant_unique UNIQUE (user_id, merchant_tenant_id)
);
