package com.yugabyte.app.yugastore.cart.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.yugabyte.app.yugastore.cart.domain.CartTenantContext;
import com.yugabyte.app.yugastore.cart.service.ShoppingCartImpl;

@ExtendWith(MockitoExtension.class)
class ShoppingCartControllerTest {

    @Mock
    private ShoppingCartImpl shoppingCart;

    @InjectMocks
    private ShoppingCartController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void addProduct_returnsAddedToCartMessage() throws Exception {
        mockMvc.perform(get("/cart-microservice/shoppingCart/addProduct")
                        .param("userid", "user1")
                        .param("asin", "B001")
                        .header(ShoppingCartController.TENANT_KEY_HEADER, "northwind-books"))
                .andExpect(status().isOk())
                .andExpect(content().string("Added to Cart"));

        verify(shoppingCart).addProductToShoppingCart("user1", "B001", "northwind-books");
    }

    @Test
    void getProductsInCart_returnsJsonMap() throws Exception {
        when(shoppingCart.getProductsInCart("user1")).thenReturn(Map.of("B001", 2, "B002", 1));

        mockMvc.perform(get("/cart-microservice/shoppingCart/productsInCart")
                        .param("userid", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.B001").value(2))
                .andExpect(jsonPath("$.B002").value(1));
    }

    @Test
    void getProductsInCart_whenCartEmpty_returnsEmptyJson() throws Exception {
        when(shoppingCart.getProductsInCart("user1")).thenReturn(Map.of());

        mockMvc.perform(get("/cart-microservice/shoppingCart/productsInCart")
                        .param("userid", "user1"))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));
    }

    @Test
    void getCartTenantContext_returnsTenantKeyJson() throws Exception {
        when(shoppingCart.getCartTenantContext("user1")).thenReturn(new CartTenantContext("northwind-books"));

        mockMvc.perform(get("/cart-microservice/shoppingCart/tenantContext")
                        .param("userid", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantKey").value("northwind-books"));
    }

    @Test
    void removeProduct_returnsRemovingFromCartMessage() throws Exception {
        mockMvc.perform(get("/cart-microservice/shoppingCart/removeProduct")
                        .param("userid", "user1")
                        .param("asin", "B001"))
                .andExpect(status().isOk())
                .andExpect(content().string("Removing from Cart"));

        verify(shoppingCart).removeProductFromCart("user1", "B001");
    }

    @Test
    void clearCart_returnsClearingCartMessage() throws Exception {
        mockMvc.perform(get("/cart-microservice/shoppingCart/clearCart")
                        .param("userid", "user1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Clearing Cart, Checkout successful"));

        verify(shoppingCart).clearCart("user1");
    }
}
