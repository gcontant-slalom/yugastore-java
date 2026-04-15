package com.yugabyte.app.yugastore.cart.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import com.yugabyte.app.yugastore.cart.domain.CartTenantContext;
import com.yugabyte.app.yugastore.cart.domain.ShoppingCart;
import com.yugabyte.app.yugastore.cart.domain.ShoppingCartKey;
import com.yugabyte.app.yugastore.cart.repositories.ShoppingCartRepository;

@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Transactional
public class ShoppingCartImpl {
	
	private static final int DEFAULT_QUANTITY = 1;
	private static final String CART_TENANT_CONTEXT_MISMATCH_MESSAGE = "Cart items must all belong to the same tenant context.";
	private final ShoppingCartRepository shoppingCartRepository;

	@Autowired
	public ShoppingCartImpl(ShoppingCartRepository shoppingCartRepository) {
		this.shoppingCartRepository = shoppingCartRepository;
	}

	/**
	 * If product is in the map just increment quantity by 1. If product is not in
	 * the map with, add it with quantity 1
	 *
	 * @param product
	 */
	@SuppressWarnings("null")
	public void addProductToShoppingCart(String userId, String asin, String tenantKey) {

		List<ShoppingCart> existingCartItems = getCartItems(userId);
		if (!existingCartItems.isEmpty()) {
			String existingTenantKey = resolveCartTenantKey(existingCartItems);
			if (!sameTenantContext(existingTenantKey, tenantKey)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, CART_TENANT_CONTEXT_MISMATCH_MESSAGE);
			}
		}

		ShoppingCartKey currentKey = new ShoppingCartKey(userId, asin);
		String shoppingCartKeyStr = userId + "-" + asin;
		if (shoppingCartRepository.findById(shoppingCartKeyStr).isPresent()) {
			shoppingCartRepository.updateQuantityForShoppingCart(userId, asin);
			System.out.println("Adding product: " + asin);
		} else {
			ShoppingCart currentShoppingCart = createCartObject(currentKey, normalizeTenantKey(tenantKey));
			shoppingCartRepository.save(currentShoppingCart);
			System.out.println("Adding product: " + asin);
		}
	}

	public Map<String, Integer> getProductsInCart(String userId) {

		Map<String, Integer> productsInCartAsin = new HashMap<String, Integer>();

		List<ShoppingCart> productsInCart = getCartItems(userId);
		if (!productsInCart.isEmpty()) {
			for (ShoppingCart item : productsInCart) {
				productsInCartAsin.put(item.getAsin(), item.getQuantity());
			}

		}
		return productsInCartAsin;
	}

	public CartTenantContext getCartTenantContext(String userId) {
		return new CartTenantContext(resolveCartTenantKey(getCartItems(userId)));
	}

	public void removeProductFromCart(String userId, String asin) {
		String shoppingCartKeyStr = userId + "-" + asin;
		if (shoppingCartRepository.findById(shoppingCartKeyStr).isPresent()) {
			if (shoppingCartRepository.findById(shoppingCartKeyStr).get().getQuantity() > 1) {
				shoppingCartRepository.decrementQuantityForShoppingCart(userId, asin);
				System.out.println("Decrementing product: " + asin + " quantity");
			} else if (shoppingCartRepository.findById(shoppingCartKeyStr).get().getQuantity() == 1) {
				shoppingCartRepository.deleteById(shoppingCartKeyStr);
				System.out.println("Removing product: " + asin + " since it was qty 1");
			}
		}
	}

	private @NonNull ShoppingCart createCartObject(ShoppingCartKey currentKey, String tenantKey) {
		ShoppingCart currentShoppingCart = new ShoppingCart();
		currentShoppingCart.setCartKey(currentKey.getId() + "-" + currentKey.getAsin());
		currentShoppingCart.setUserId(currentKey.getId());
		currentShoppingCart.setAsin(currentKey.getAsin());
		currentShoppingCart.setTenantKey(tenantKey);
		LocalDateTime currentTime = LocalDateTime.now();
		currentShoppingCart.setTime_added(currentTime.toString());
		currentShoppingCart.setQuantity(DEFAULT_QUANTITY);

		return currentShoppingCart;
	}

	public void clearCart(String userId) {

		if (shoppingCartRepository.findProductsInCartByUserId(userId).isPresent()) {
			shoppingCartRepository.deleteProductsInCartByUserId(userId);
			System.out.println("Deleteing all products for user: " + userId + " since checkout was successful");
		}
	}

	private List<ShoppingCart> getCartItems(String userId) {
		return shoppingCartRepository.findProductsInCartByUserId(userId).orElse(List.of());
	}

	private String resolveCartTenantKey(List<ShoppingCart> cartItems) {
		Set<String> tenantKeys = new HashSet<String>();
		for (ShoppingCart cartItem : cartItems) {
			String tenantKey = normalizeTenantKey(cartItem.getTenantKey());
			if (tenantKey != null) {
				tenantKeys.add(tenantKey);
			}
		}

		if (tenantKeys.size() > 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, CART_TENANT_CONTEXT_MISMATCH_MESSAGE);
		}

		return tenantKeys.isEmpty() ? null : tenantKeys.iterator().next();
	}

	private boolean sameTenantContext(String leftTenantKey, String rightTenantKey) {
		String normalizedLeft = normalizeTenantKey(leftTenantKey);
		String normalizedRight = normalizeTenantKey(rightTenantKey);
		if (normalizedLeft == null && normalizedRight == null) {
			return true;
		}
		if (normalizedLeft == null || normalizedRight == null) {
			return false;
		}
		return normalizedLeft.equals(normalizedRight);
	}

	private String normalizeTenantKey(String tenantKey) {
		if (tenantKey == null || tenantKey.isBlank()) {
			return null;
		}
		return tenantKey;
	}

}
