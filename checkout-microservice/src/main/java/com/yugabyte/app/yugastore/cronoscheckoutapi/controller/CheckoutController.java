package com.yugabyte.app.yugastore.cronoscheckoutapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yugabyte.app.yugastore.cronoscheckoutapi.domain.CheckoutStatus;
import com.yugabyte.app.yugastore.cronoscheckoutapi.domain.Order;
import com.yugabyte.app.yugastore.cronoscheckoutapi.exception.NotEnoughProductsInStockException;
import com.yugabyte.app.yugastore.cronoscheckoutapi.service.CheckoutServiceImpl;


@RestController
@RequestMapping(value = "/checkout-microservice")
public class CheckoutController {
	
	CheckoutServiceImpl checkoutService;
	
	public CheckoutController(CheckoutServiceImpl checkoutService) {
		this.checkoutService = checkoutService;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/shoppingCart/checkout", produces = "application/json")
	public CheckoutStatus checkout(
			@RequestParam("userid") String userId,
			@RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey,
			@RequestHeader(value = "X-Merchant-Company-Name", required = false) String companyName) {
		CheckoutStatus checkoutStatus = new CheckoutStatus();
		try {
			Order currentOrder = checkoutService.checkout(userId, tenantKey, companyName);
			if (currentOrder != null) {
				checkoutStatus.setOrderNumber(currentOrder.getId().toString());
				checkoutStatus.setStatus(CheckoutStatus.SUCCESS);
				checkoutStatus.setOrderDetails(currentOrder.getOrder_details());
				System.out
						.println("Order is : " + currentOrder.getId() + " Details: " + currentOrder.getOrder_details());
			} else {
				checkoutStatus.setOrderNumber("");
				checkoutStatus.setStatus(CheckoutStatus.FAILURE);
				checkoutStatus.setOrderDetails("Product is Out of Stock!");
			}
		} catch (NotEnoughProductsInStockException e) {
			checkoutStatus.setOrderNumber("");
			checkoutStatus.setStatus(CheckoutStatus.FAILURE);
			return checkoutStatus;
		}
		return checkoutStatus;
	}	
}
