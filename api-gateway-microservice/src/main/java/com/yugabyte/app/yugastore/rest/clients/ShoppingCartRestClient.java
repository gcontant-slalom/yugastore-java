package com.yugabyte.app.yugastore.rest.clients;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.yugabyte.app.yugastore.domain.CartTenantContext;

@FeignClient("cart-microservice")
public interface ShoppingCartRestClient {

  @RequestMapping("/cart-microservice/shoppingCart/addProduct")
  String addProductToCart(@RequestParam("userid") String userId,
    @RequestParam("asin") String asin,
    @RequestHeader(value = "X-Tenant-Key", required = false) String tenantKey);

  @RequestMapping("/cart-microservice/shoppingCart/productsInCart")
  Map<String, Integer> getProductsInCart(@RequestParam("userid") String userId);

  @RequestMapping("/cart-microservice/shoppingCart/tenantContext")
  CartTenantContext getCartTenantContext(@RequestParam("userid") String userId);

  @RequestMapping("/cart-microservice/shoppingCart/removeProduct")
  String removeProductFromCart(@RequestParam("userid") String userId,
    @RequestParam("asin") String asin);

  @RequestMapping("/cart-microservice/shoppingCart/clearCart")
  String clearCart(@RequestParam("userid") String userId);
}
