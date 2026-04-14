package com.yugabyte.app.yugastore.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.yugabyte.app.yugastore.domain.CheckoutStatus;
import com.yugabyte.app.yugastore.service.AuthServiceRest;
import com.yugabyte.app.yugastore.service.CheckoutServiceRest;
import com.yugabyte.app.yugastore.service.ShoppingCartServiceRest;

@RestController
@RequestMapping(value = "/api/v1")
public class ShoppingCartController {

	private final ShoppingCartServiceRest shoppingCartServiceRest;

	private final CheckoutServiceRest checkoutServiceRest;

	private final AuthServiceRest authServiceRest;

	@Autowired
	public ShoppingCartController(ShoppingCartServiceRest shoppingCartServiceRest,
			CheckoutServiceRest checkoutServiceRest,
			AuthServiceRest authServiceRest) {
		this.shoppingCartServiceRest = shoppingCartServiceRest;
		this.checkoutServiceRest = checkoutServiceRest;
		this.authServiceRest = authServiceRest;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/shoppingCart", produces = "application/json")
	public @ResponseBody ResponseEntity<Map<String, Integer>> shoppingCart() {

		String userId = currentUserId();
		Map<String, Integer> productsInCart = shoppingCartServiceRest.getProductsInCart(userId);

		if (productsInCart == null) {
			return new ResponseEntity<Map<String, Integer>>(productsInCart, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Map<String, Integer>>(productsInCart, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/shoppingCart/addProduct", produces = "application/json")
	public ResponseEntity<?> addProductToCart(@RequestParam("asin") String asin) {
		String userId = currentUserId();
		shoppingCartServiceRest.addProduct(userId, asin);
		Map<String, Integer> productsInCart = shoppingCartServiceRest.getProductsInCart(userId);

		if (productsInCart == null) {
			return new ResponseEntity<Map<String, Integer>>(productsInCart, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Map<String, Integer>>(productsInCart, HttpStatus.OK);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/shoppingCart/removeProduct", produces = "application/json")
	public ResponseEntity<Map<String, Integer>> removeProductFromCart(@RequestParam("asin") String asin) {
		String userId = currentUserId();
		shoppingCartServiceRest.removeProduct(userId, asin);
		Map<String, Integer> productsInCart = shoppingCartServiceRest.getProductsInCart(userId);

		if (productsInCart == null) {
			return new ResponseEntity<Map<String, Integer>>(productsInCart, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Map<String, Integer>>(productsInCart, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/shoppingCart/checkout", produces = "application/json")
	public ResponseEntity<CheckoutStatus> checkout() {
		String userId = currentUserId();
		CheckoutStatus checkoutStatus = checkoutServiceRest.checkout(userId);
		return new ResponseEntity<CheckoutStatus>(checkoutStatus, HttpStatus.OK);
	}

	private String currentUserId() {
		return authServiceRest.currentUser().getUserId();
	}

}
