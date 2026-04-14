package com.yugabyte.yugastore.ui.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

@Component
public class DashboardRestConsumer {
	private static final Set<String> PROXIED_RESPONSE_HEADERS_TO_DROP = new HashSet<>(Arrays.asList(
			HttpHeaders.TRANSFER_ENCODING,
			HttpHeaders.CONTENT_LENGTH,
			HttpHeaders.CONNECTION,
			"Keep-Alive",
			HttpHeaders.TE,
			HttpHeaders.TRAILER,
			HttpHeaders.UPGRADE,
			HttpHeaders.PROXY_AUTHENTICATE,
			HttpHeaders.PROXY_AUTHORIZATION,
			HttpHeaders.SET_COOKIE,
			HttpHeaders.CONTENT_ENCODING));

	@Autowired
	RestTemplate restTemplate;

	public static final String AUTH_USER_ID_HEADER = "X-Authenticated-UserId";
	
	@Value("${cronos.yugabyte.api:http://localhost:8081/api/v1}")
	String restUrlBase;

	public String getHomePageProducts(int limit, int offset) {

		String restURL = restUrlBase + "products?limit=" + limit + "&offset=" + offset;
		ResponseEntity<String> rateResponse =
		        restTemplate.exchange(restURL,
		                    HttpMethod.GET, null, String.class);
		String productListJsonResponse = rateResponse.getBody();

		JsonElement productListJsonArray =
			new Gson().fromJson(productListJsonResponse, JsonArray.class);
		return productListJsonArray.toString();
	}

	public String getProductsByCategory(String name, int limit, int offset) {
		String encodedName = name.replace(" ","%20").replace("&","%26").replace(",","%2C");
		String restURL = restUrlBase + "products/category/" + encodedName+ "?limit=" + limit + "&offset=" + offset;
		System.out.println(restURL);
		ResponseEntity<String> rateResponse =
		        restTemplate.exchange(restURL,
		                    HttpMethod.GET, null, String.class);
		String productListJsonResponse = rateResponse.getBody();

		JsonElement productListJsonArray =
			new Gson().fromJson(productListJsonResponse, JsonArray.class);
		return productListJsonArray.toString();
	}

	/**
	 * Loads the details page of any product.
	 */
	public String getProductDetails(String asin) {

		String restURL = restUrlBase + "product/" + asin;
		ResponseEntity<String> rateResponse =
		  restTemplate.exchange(
		  	restURL,
				HttpMethod.GET, null, String.class);
		String productDetailsJsonResponse = rateResponse.getBody();
		return productDetailsJsonResponse;
	}	

	public String addProductToCart(String asin) {
		return addProductToCart(asin, null).getBody();
	}

	public ResponseEntity<String> addProductToCart(String asin, String userId) {

		String restURL = restUrlBase + "shoppingCart/addProduct";
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("asin", asin);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(params,
				buildHeaders(userId, MediaType.APPLICATION_FORM_URLENCODED));

		return exchange(restURL, HttpMethod.POST, request);
	}

	public String removeProductFromCart(String asin) {
		return removeProductFromCart(asin, null).getBody();
	}

	public ResponseEntity<String> removeProductFromCart(String asin, String userId) {

		String restURL = restUrlBase + "shoppingCart/removeProduct";
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("asin", asin);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(params,
				buildHeaders(userId, MediaType.APPLICATION_FORM_URLENCODED));

		return exchange(restURL, HttpMethod.POST, request);
	}

	public String getCart() {
		return getCart(null).getBody();
	}

	public ResponseEntity<String> getCart(String userId) {
		String restURL = restUrlBase + "shoppingCart";
		HttpEntity<Void> request = new HttpEntity<Void>(buildHeaders(userId, MediaType.APPLICATION_JSON));
		return exchange(restURL, HttpMethod.POST, request);
	}

	public String checkout() {
		return checkout(null).getBody();
	}

	public ResponseEntity<String> checkout(String userId) {

		String restURL = restUrlBase + "shoppingCart/checkout";
		HttpEntity<Void> request = new HttpEntity<Void>(buildHeaders(userId, MediaType.APPLICATION_JSON));
		return exchange(restURL, HttpMethod.POST, request);
	}

	public String showCart() {
		return showCart(null).getBody();
	}

	public ResponseEntity<String> showCart(String userId) {
		return getCart(userId);
	}

	public ResponseEntity<String> register(String payload) {
		String restURL = restUrlBase + "auth/register";
		HttpEntity<String> request = new HttpEntity<String>(payload, buildHeaders(null, MediaType.APPLICATION_JSON));
		return exchange(restURL, HttpMethod.POST, request);
	}

	public ResponseEntity<String> login(String payload) {
		String restURL = restUrlBase + "auth/login";
		HttpEntity<String> request = new HttpEntity<String>(payload, buildHeaders(null, MediaType.APPLICATION_JSON));
		return exchange(restURL, HttpMethod.POST, request);
	}

	public ResponseEntity<String> logout() {
		String restURL = restUrlBase + "auth/logout";
		HttpEntity<Void> request = new HttpEntity<Void>(buildHeaders(null, MediaType.APPLICATION_JSON));
		return exchange(restURL, HttpMethod.POST, request);
	}

	private HttpHeaders buildHeaders(String userId, MediaType contentType) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		if (contentType != null) {
			headers.setContentType(contentType);
		}
		if (userId != null && !userId.isBlank()) {
			headers.add(AUTH_USER_ID_HEADER, userId);
		}
		return headers;
	}

	private ResponseEntity<String> exchange(String url, HttpMethod method, HttpEntity<?> request) {
		try {
			ResponseEntity<String> response = restTemplate.exchange(url, method, request, String.class);
			return ResponseEntity.status(response.getStatusCode())
					.headers(sanitizeHeaders(response.getHeaders()))
					.body(response.getBody());
		} catch (HttpStatusCodeException ex) {
			return ResponseEntity.status(ex.getStatusCode()).headers(sanitizeHeaders(ex.getResponseHeaders()))
					.body(ex.getResponseBodyAsString());
		}
	}

	private HttpHeaders sanitizeHeaders(HttpHeaders sourceHeaders) {
		HttpHeaders sanitizedHeaders = new HttpHeaders();
		if (sourceHeaders == null) {
			return sanitizedHeaders;
		}

		sourceHeaders.forEach((headerName, headerValues) -> {
			if (!PROXIED_RESPONSE_HEADERS_TO_DROP.contains(headerName)) {
				sanitizedHeaders.put(headerName, new ArrayList<>(headerValues));
			}
		});

		return sanitizedHeaders;
	}
}
