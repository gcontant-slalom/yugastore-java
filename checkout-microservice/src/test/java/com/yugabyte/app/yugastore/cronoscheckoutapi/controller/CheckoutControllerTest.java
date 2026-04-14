package com.yugabyte.app.yugastore.cronoscheckoutapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.yugabyte.app.yugastore.cronoscheckoutapi.domain.CheckoutStatus;
import com.yugabyte.app.yugastore.cronoscheckoutapi.domain.Order;
import com.yugabyte.app.yugastore.cronoscheckoutapi.exception.NotEnoughProductsInStockException;
import com.yugabyte.app.yugastore.cronoscheckoutapi.service.CheckoutServiceImpl;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private CheckoutServiceImpl checkoutService;

    @InjectMocks
    private CheckoutController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void checkout_whenOrderSucceeds_returnsSuccessStatus() throws Exception {
        Order order = buildOrder("order-123", "Customer bought: Gadget X", 49.99);
        when(checkoutService.checkout("u1001")).thenReturn(order);

        mockMvc.perform(post("/checkout-microservice/shoppingCart/checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(CheckoutStatus.SUCCESS))
                .andExpect(jsonPath("$.orderNumber").value("order-123"))
                .andExpect(jsonPath("$.orderDetails").value("Customer bought: Gadget X"));
    }

    @Test
    void checkout_whenCartIsEmpty_returnsFailureStatus() throws Exception {
        when(checkoutService.checkout("u1001")).thenReturn(null);

        mockMvc.perform(post("/checkout-microservice/shoppingCart/checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(CheckoutStatus.FAILURE))
                .andExpect(jsonPath("$.orderNumber").value(""))
                .andExpect(jsonPath("$.orderDetails").value("Product is Out of Stock!"));
    }

    @Test
    void checkout_whenNotEnoughStock_returnsFailureStatus() throws Exception {
        when(checkoutService.checkout("u1001"))
                .thenThrow(new NotEnoughProductsInStockException("Gadget X", 1));

        mockMvc.perform(post("/checkout-microservice/shoppingCart/checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(CheckoutStatus.FAILURE))
                .andExpect(jsonPath("$.orderNumber").value(""));
    }

    private Order buildOrder(String id, String details, double total) {
        Order order = new Order();
        order.setId(id);
        order.setOrder_details(details);
        order.setOrder_total(total);
        order.setUser_id(1);
        return order;
    }
}
