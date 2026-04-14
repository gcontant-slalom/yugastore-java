package com.yugabyte.app.yugastore.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yugabyte.app.yugastore.rest.clients.ShoppingCartRestClient;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceRestImplTest {

    @Mock
    private ShoppingCartRestClient shoppingCartRestClient;

    private ShoppingCartServiceRestImpl service;

    @BeforeEach
    void setUp() {
        service = new ShoppingCartServiceRestImpl(shoppingCartRestClient);
    }

    @Test
    void addProduct_delegatesToClient() {
        when(shoppingCartRestClient.addProductToCart("u1001", "B001")).thenReturn("Added to Cart");

        String result = service.addProduct("u1001", "B001");

        assertThat(result).isEqualTo("Added to Cart");
    }

    @Test
    void getProductsInCart_delegatesToClient() {
        Map<String, Integer> cart = Map.of("B001", 2, "B002", 1);
        when(shoppingCartRestClient.getProductsInCart("u1001")).thenReturn(cart);

        Map<String, Integer> result = service.getProductsInCart("u1001");

        assertThat(result).containsEntry("B001", 2).containsEntry("B002", 1);
    }

    @Test
    void getProductsInCart_whenEmpty_returnsEmptyMap() {
        when(shoppingCartRestClient.getProductsInCart("u1001")).thenReturn(Map.of());

        Map<String, Integer> result = service.getProductsInCart("u1001");

        assertThat(result).isEmpty();
    }

    @Test
    void removeProduct_delegatesToClient() {
        when(shoppingCartRestClient.removeProductFromCart("u1001", "B001")).thenReturn("Removing from Cart");

        String result = service.removeProduct("u1001", "B001");

        assertThat(result).isEqualTo("Removing from Cart");
    }

    @Test
    void clearCart_delegatesToClient() {
        when(shoppingCartRestClient.clearCart("u1001")).thenReturn("Clearing Cart, Checkout successful");

        String result = service.clearCart("u1001");

        assertThat(result).isEqualTo("Clearing Cart, Checkout successful");
        verify(shoppingCartRestClient).clearCart("u1001");
    }
}
