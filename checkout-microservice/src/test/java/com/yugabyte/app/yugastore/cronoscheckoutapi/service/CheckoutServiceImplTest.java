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
import org.mockito.ArgumentCaptor;
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
        when(shoppingCartRestClient.getProductsInCart("42")).thenReturn(Collections.emptyMap());

        Order result = checkoutService.checkout("42");

        assertThat(result).isNull();
        verify(shoppingCartRestClient).clearCart("42");
    }

    @Test
    void checkout_whenSufficientInventory_createsOrderAndClearsCart()
            throws NotEnoughProductsInStockException {
        Map<String, Integer> cart = new HashMap<>(Map.of("B001", 2));
        ProductInventory inventory = buildInventory("B001", 10);
        ProductMetadata product = buildProduct("B001", "Gadget X", 25.0);

        when(shoppingCartRestClient.getProductsInCart("42")).thenReturn(cart);
        when(productInventoryRepository.findById("B001")).thenReturn(Optional.of(inventory));
        when(productCatalogRestClient.getProductDetails("B001")).thenReturn(product);

        Order result = checkoutService.checkout("42");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotBlank();
        assertThat(result.getUser_id()).isEqualTo(42);
        assertThat(result.getOrder_total()).isEqualTo(50.0);
        assertThat(result.getOrder_details()).contains("Gadget X");
        verify(shoppingCartRestClient).clearCart("42");
    }

    @Test
    void checkout_whenInsufficientInventory_throwsException() {
        Map<String, Integer> cart = new HashMap<>(Map.of("B001", 5));
        ProductInventory inventory = buildInventory("B001", 2);
        ProductMetadata product = buildProduct("B001", "Gadget X", 25.0);

        when(shoppingCartRestClient.getProductsInCart("42")).thenReturn(cart);
        when(productInventoryRepository.findById("B001")).thenReturn(Optional.of(inventory));
        when(productCatalogRestClient.getProductDetails("B001")).thenReturn(product);

        assertThatThrownBy(() -> checkoutService.checkout("42"))
                .isInstanceOf(NotEnoughProductsInStockException.class)
                .hasMessageContaining("Gadget X");
    }

    @Test
    void checkout_whenProductTitleContainsApostrophe_escapesCqlLiteral()
            throws NotEnoughProductsInStockException {
        Map<String, Integer> cart = new HashMap<>(Map.of("B001", 1));
        ProductInventory inventory = buildInventory("B001", 10);
        ProductMetadata product = buildProduct("B001", "Cassell's Standard Latin Dictionary", 17.96);

        when(shoppingCartRestClient.getProductsInCart("42")).thenReturn(cart);
        when(productInventoryRepository.findById("B001")).thenReturn(Optional.of(inventory));
        when(productCatalogRestClient.getProductDetails("B001")).thenReturn(product);

        checkoutService.checkout("42");

        ArgumentCaptor<String> statementCaptor = ArgumentCaptor.forClass(String.class);
        verify(cqlOperations).execute(statementCaptor.capture());
        assertThat(statementCaptor.getValue()).contains("Cassell''s Standard Latin Dictionary");
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
