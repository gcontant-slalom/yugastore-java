package com.yugabyte.app.yugastore.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.web.server.ResponseStatusException;

import com.yugabyte.app.yugastore.domain.AuthUser;
import com.yugabyte.app.yugastore.domain.CheckoutStatus;
import com.yugabyte.app.yugastore.service.AuthServiceRest;
import com.yugabyte.app.yugastore.service.CheckoutServiceRest;
import com.yugabyte.app.yugastore.service.ShoppingCartServiceRest;

@ExtendWith(MockitoExtension.class)
class ShoppingCartControllerTest {

    @Mock
    private ShoppingCartServiceRest shoppingCartServiceRest;

    @Mock
    private CheckoutServiceRest checkoutServiceRest;

    @Mock
    private AuthServiceRest authServiceRest;

    @InjectMocks
    private ShoppingCartController controller;

    private MockMvc mockMvc;

    private AuthUser currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new AuthUser();
        currentUser.setUserId("42");
        currentUser.setEmail("merchant@example.com");
        when(authServiceRest.currentUser()).thenReturn(currentUser);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shoppingCart_returnsCartContents() throws Exception {
        when(shoppingCartServiceRest.getProductsInCart("42")).thenReturn(Map.of("B001", 2));

        mockMvc.perform(post("/api/v1/shoppingCart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.B001").value(2));
    }

    @Test
    void shoppingCart_whenNull_returns500() throws Exception {
        when(shoppingCartServiceRest.getProductsInCart("42")).thenReturn(null);

        mockMvc.perform(post("/api/v1/shoppingCart"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addProductToCart_returnsUpdatedCart() throws Exception {
        when(shoppingCartServiceRest.getProductsInCart("42")).thenReturn(Map.of("B001", 1));

        mockMvc.perform(post("/api/v1/shoppingCart/addProduct")
                        .param("asin", "B001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.B001").value(1));

        verify(shoppingCartServiceRest).addProduct("42", "B001");
    }

    @Test
    void addProductToCart_whenNull_returns500() throws Exception {
        when(shoppingCartServiceRest.getProductsInCart("42")).thenReturn(null);

        mockMvc.perform(post("/api/v1/shoppingCart/addProduct")
                        .param("asin", "B001"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void removeProductFromCart_returnsUpdatedCart() throws Exception {
        when(shoppingCartServiceRest.getProductsInCart("42")).thenReturn(Map.of("B002", 3));

        mockMvc.perform(post("/api/v1/shoppingCart/removeProduct")
                        .param("asin", "B001"))
                .andExpect(status().isOk());

        verify(shoppingCartServiceRest).removeProduct("42", "B001");
    }

    @Test
    void removeProductFromCart_whenNull_returns500() throws Exception {
        when(shoppingCartServiceRest.getProductsInCart("42")).thenReturn(null);

        mockMvc.perform(post("/api/v1/shoppingCart/removeProduct")
                        .param("asin", "B001"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void checkout_returnsCheckoutStatus() throws Exception {
        CheckoutStatus checkoutStatus = new CheckoutStatus();
        checkoutStatus.setStatus(CheckoutStatus.SUCCESS);
        checkoutStatus.setOrderNumber("order-abc");
        when(checkoutServiceRest.checkout("42")).thenReturn(checkoutStatus);

        mockMvc.perform(post("/api/v1/shoppingCart/checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(CheckoutStatus.SUCCESS))
                .andExpect(jsonPath("$.orderNumber").value("order-abc"));
    }

        @Test
        void shoppingCart_whenUnauthenticated_returns401() throws Exception {
        when(authServiceRest.currentUser())
            .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED,
                "No authenticated user."));

        mockMvc.perform(post("/api/v1/shoppingCart"))
            .andExpect(status().isUnauthorized());

        verify(shoppingCartServiceRest, never()).getProductsInCart("42");
        }
}
