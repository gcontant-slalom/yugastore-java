package com.yugabyte.app.yugastore.service;

import java.util.Map;

import com.yugabyte.app.yugastore.domain.CartTenantContext;

public interface ShoppingCartServiceRest {
	
	String addProduct(String userId, String product, String tenantKey);
	Map<String, Integer> getProductsInCart(String userId);
	CartTenantContext getCartTenantContext(String userId);
	String removeProduct(String userId, String product);
	String clearCart(String userId);
}
