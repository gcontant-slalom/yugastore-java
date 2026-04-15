CREATE TABLE IF NOT EXISTS "user" (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL
);

CREATE TABLE shopping_cart(
  cart_key TEXT NOT NULL,
  user_id TEXT NOT NULL,
  asin TEXT NOT NULL,
  tenant_key TEXT,
  time_added TEXT NOT NULL,
  quantity INT NOT NULL,
  PRIMARY KEY (cart_key)
);