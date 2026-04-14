package com.yugabyte.app.yugastore.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yugabyte.app.yugastore.domain.CheckoutStatus;
import com.yugabyte.app.yugastore.rest.clients.CheckoutRestClient;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceRestImplTest {

    @Mock
    private CheckoutRestClient checkoutRestClient;

    private CheckoutServiceRestImpl service;

    @BeforeEach
    void setUp() {
        service = new CheckoutServiceRestImpl(checkoutRestClient);
    }

    @Test
    void checkout_whenSuccess_returnsSuccessStatus() {
        CheckoutStatus expected = new CheckoutStatus();
        expected.setStatus(CheckoutStatus.SUCCESS);
        expected.setOrderNumber("order-abc");
        when(checkoutRestClient.checkout()).thenReturn(expected);

        CheckoutStatus result = service.checkout();

        assertThat(result.getStatus()).isEqualTo(CheckoutStatus.SUCCESS);
        assertThat(result.getOrderNumber()).isEqualTo("order-abc");
    }

    @Test
    void checkout_whenFailure_returnsFailureStatus() {
        CheckoutStatus expected = new CheckoutStatus();
        expected.setStatus(CheckoutStatus.FAILURE);
        expected.setOrderNumber("");
        when(checkoutRestClient.checkout()).thenReturn(expected);

        CheckoutStatus result = service.checkout();

        assertThat(result.getStatus()).isEqualTo(CheckoutStatus.FAILURE);
        assertThat(result.getOrderNumber()).isEmpty();
    }
}
