package com.yugabyte.app.yugastore.cart.domain;

public class CartTenantContext {

	private String tenantKey;

	public CartTenantContext() {
	}

	public CartTenantContext(String tenantKey) {
		this.tenantKey = tenantKey;
	}

	public String getTenantKey() {
		return tenantKey;
	}

	public void setTenantKey(String tenantKey) {
		this.tenantKey = tenantKey;
	}
}