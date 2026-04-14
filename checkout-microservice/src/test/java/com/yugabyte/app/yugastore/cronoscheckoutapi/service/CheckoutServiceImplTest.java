package com.yugabyte.app.yugastore.cronoscheckoutapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.cql.CqlOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.yugabyte.app.yugastore.cronoscheckoutapi.domain.Order;
import com.yugabyte.app.yugastore.cronoscheckoutapi.domain.ProductInventory;
import com.yugabyte.app.yugastore.cronoscheckoutapi.domain.ProductMetadata;
import com.yugabyte.app.yugastore.cronoscheckoutapi.exception.NotEnoughProductsInStockException;
import com.yugabyte.app.yugastore.cronoscheckoutapi.repositories.ProductInventoryRepository;
import com.yugabyte.app.yugastore.cronoscheckoutapi.rest.clients.ProductCatalogRestClient;
import com.yugabyte.app.yugastore.cronoscheckoutapi.rest.clients.ShoppingCartRestClient;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CheckoutServiceImplTest {

    @Mock
    private ProductInventoryRepository productInventoryRepository;
    @Mock
    private ShoppingCartRestClient shoppingCartRestClient;
    @Mock
    private ProductCatalogRestClient productCatalogRestClient;
    @Mock
    private CassandraOperations cassandraTemplate;
    @Mock
    private CqlOperations cqlOperations;

    private CheckoutServiceImpl checkoutService;

    @BeforeEach
    void setUp() {
        checkoutService = new CheckoutServiceImpl(productInventoryRepository, shoppingCartRestClient,
                productCatalogRestClient);
        ReflectionTestUtils.setField(checkoutService, "cassandraTemplate", cassandraTemplate);
        when(cassandraTemplate.getCqlOperations()).thenReturn(cqlOperations);
    }

    @Test
    void checkout_whenCartIsEmpty_returnsNull() throws NotEnoughProductsInStockException {
        when(shoppingCartRestClient.getProductsInCart("u1001")).thenReturn(Collections.emptyMap());

        Order result = checkoutService.checkout("u1001");

        assertThat(result).isNull();
        verify(shoppingCartRestClient).clearCart("u1001");
    }

    @Test
    void checkout_whenSufficientInventory_createsOrderAndClearsCart()
            throws NotEnoughProductsInStockException {
        Map<String, Integer> cart = new HashMap<>(Map.of("B001", 2));
        ProductInventory inventory = buildInventory("B001", 10);
        ProductMetadata product = buildProduct("B001", "Gadget X", 25.0);

        when(shoppingCartRestClient.getProductsInCart("u1001")).thenReturn(cart);
        when(productInventoryRepository.findById("B001")).thenReturn(Optional.of(inventory));
        when(productCatalogRestClient.getProductDetails("B001")).thenReturn(product);

        Order result = checkoutService.checkout("u1001");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotBlank();
        assertThat(result.getOrder_total()).isEqualTo(50.0);
        assertThat(result.getOrder_details()).contains("Gadget X");
        verify(shoppingCartRestClient).clearCart("u1001");
    }

    @Test
    void checkout_whenInsufficientInventory_throwsException() {
        Map<String, Integer> cart = new HashMap<>(Map.of("B001", 5));
        ProductInventory inventory = buildInventory("B001", 2);
        ProductMetadata product = buildProduct("B001", "Gadget X", 25.0);

        when(shoppingCartRestClient.getProductsInCart("u1001")).thenReturn(cart);
        when(productInventoryRepository.findById("B001")).thenReturn(Optional.of(inventory));
        when(productCatalogRestClient.getProductDetails("B001")).thenReturn(product);

        assertThatThrownBy(() -> checkoutService.checkout("u1001"))
                .isInstanceOf(NotEnoughProductsInStockException.class)
                .hasMessageContaining("Gadget X");
    }

    private ProductInventory buildInventory(String id, int quantity) {
        ProductInventory inv = new ProductInventory();
        inv.setId(id);
        inv.setQuantity(quantity);
        return inv;
    }

    private ProductMetadata buildProduct(String id, String title, double price) {
        ProductMetadata p = new ProductMetadata();
        p.setId(id);
        p.setTitle(title);
        p.setPrice(price);
        return p;
    }
}
