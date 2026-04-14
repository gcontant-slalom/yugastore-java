package com.yugabyte.yugastore.ui.controller;

import com.google.gson.Gson;
import com.yugabyte.yugastore.ui.model.AuthUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

import com.yugabyte.yugastore.ui.rest.DashboardRestConsumer;

@RestController
public class CronosProductsController {

	private static final String AUTH_USER_SESSION_KEY = "authUser";
	
	@Autowired
	DashboardRestConsumer dashboardRestConsumer;

	@GetMapping("/products")
	public String getProductDetails() {
		return dashboardRestConsumer.getHomePageProducts(10, 0);
		//return "[{\"id\": 5,\"name\": \"How to Win Friends & Influence People\",\"description\": \"For more than sixty years the rock-solid\",\"price\": 9.6,\"author\": \"Dale Carnegie\",\"type\": \"paperback\",\"img\": \"https://images-na.ssl-images-amazon.com/images/I/51PWIy1rHUL._AA300_.jpg\",\"category\": \"business\",\"num_reviews\": 182,\"total_stars\": 550,\"stars\": \"3.02\"}]";
	}

	@GetMapping("/products/category/{category}")
	public String getProductDetails(@PathVariable("category") String category, @RequestParam("limit") int limit, @RequestParam("offset") int offset) {
		return dashboardRestConsumer.getProductsByCategory(category, limit, offset);
	}

  @RequestMapping(method = RequestMethod.GET, value = "/products/details")
  public @ResponseBody String getProductDetails(@RequestParam("asin") String asin) {
    return dashboardRestConsumer.getProductDetails(asin);
  }

  @PostMapping("/cart/add")
	public ResponseEntity<String> addProductToCart(@RequestParam("asin") String asin, HttpSession session) {
		AuthUser authUser = requireAuthenticatedUser(session);
		return dashboardRestConsumer.addProductToCart(asin, authUser.getUserId());
	}
  
  @PostMapping("/cart/get")
	public ResponseEntity<String> showCart(HttpSession session) {
		AuthUser authUser = requireAuthenticatedUser(session);
		return dashboardRestConsumer.showCart(authUser.getUserId());
	}
  
  @PostMapping("/cart/checkout")
	public ResponseEntity<String> checkoutCart(HttpSession session) {
		AuthUser authUser = requireAuthenticatedUser(session);
		return dashboardRestConsumer.checkout(authUser.getUserId());
	}

  @RequestMapping(method = RequestMethod.POST, value = "/cart/remove")
	public ResponseEntity<String> removeProductFromCart(@RequestParam("asin") String asin, HttpSession session) {
		AuthUser authUser = requireAuthenticatedUser(session);
		return dashboardRestConsumer.removeProductFromCart(asin, authUser.getUserId());
	}

  @RequestMapping(method = RequestMethod.POST, value = "/cart/getCart")
	public ResponseEntity<String> getCart(HttpSession session) {
		AuthUser authUser = requireAuthenticatedUser(session);
		return dashboardRestConsumer.getCart(authUser.getUserId());
	}

	@PostMapping("/auth/register")
	public ResponseEntity<String> register(@RequestBody String payload, HttpSession session) {
		ResponseEntity<String> response = dashboardRestConsumer.register(payload);
		storeAuthUserIfSuccessful(session, response);
		return response;
	}

	@PostMapping("/auth/login")
	public ResponseEntity<String> login(@RequestBody String payload, HttpSession session) {
		ResponseEntity<String> response = dashboardRestConsumer.login(payload);
		storeAuthUserIfSuccessful(session, response);
		return response;
	}

	@PostMapping("/auth/logout")
	public ResponseEntity<String> logout(HttpSession session) {
		dashboardRestConsumer.logout();
		session.removeAttribute(AUTH_USER_SESSION_KEY);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/auth/current-user")
	public ResponseEntity<AuthUser> currentUser(HttpSession session) {
		AuthUser authUser = (AuthUser) session.getAttribute(AUTH_USER_SESSION_KEY);
		if (authUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(authUser);
	}

	private AuthUser requireAuthenticatedUser(HttpSession session) {
		AuthUser authUser = (AuthUser) session.getAttribute(AUTH_USER_SESSION_KEY);
		if (authUser == null) {
			throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user.");
		}
		return authUser;
	}

	private void storeAuthUserIfSuccessful(HttpSession session, ResponseEntity<String> response) {
		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().isBlank()) {
			return;
		}
		AuthUser authUser = new Gson().fromJson(response.getBody(), AuthUser.class);
		if (authUser != null && authUser.getUserId() != null) {
			session.setAttribute(AUTH_USER_SESSION_KEY, authUser);
		}
	}
}
