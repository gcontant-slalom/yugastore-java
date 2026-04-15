package com.yugabyte.app.yugastore.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.yugabyte.app.yugastore.cart.domain.CartTenantContext;
import com.yugabyte.app.yugastore.cart.domain.ShoppingCart;
import com.yugabyte.app.yugastore.cart.repositories.ShoppingCartRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ShoppingCartImplTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    private ShoppingCartImpl shoppingCart;

    @BeforeEach
    void setUp() {
        shoppingCart = new ShoppingCartImpl(shoppingCartRepository);
    }

    // --- addProductToShoppingCart ---

    @Test
    void addProduct_whenProductAlreadyInCart_incrementsQuantity() {
        String userId = "user1";
        String asin = "B001";
        String key = userId + "-" + asin;
        when(shoppingCartRepository.findById(key)).thenReturn(Optional.of(buildCart(userId, asin, 2)));
        when(shoppingCartRepository.findProductsInCartByUserId(userId)).thenReturn(Optional.of(List.of(buildCart(userId, asin, 2))));

        shoppingCart.addProductToShoppingCart(userId, asin, null);

        verify(shoppingCartRepository).updateQuantityForShoppingCart(userId, asin);
        verify(shoppingCartRepository, never()).save(org.mockito.ArgumentMatchers.<ShoppingCart>any());
    }

    @Test
    void addProduct_whenProductNotInCart_savesNewItemWithQuantityOne() {
        String userId = "user1";
        String asin = "B001";
        String key = userId + "-" + asin;
        when(shoppingCartRepository.findById(key)).thenReturn(Optional.empty());
        when(shoppingCartRepository.findProductsInCartByUserId(userId)).thenReturn(Optional.empty());

        shoppingCart.addProductToShoppingCart(userId, asin, "northwind-books");

        verify(shoppingCartRepository, never()).updateQuantityForShoppingCart(any(), any());
        verify(shoppingCartRepository).save(argThat((ShoppingCart sc) ->
                sc.getUserId().equals(userId)
                        && sc.getAsin().equals(asin)
                && "northwind-books".equals(sc.getTenantKey())
                        && sc.getQuantity() == 1
                        && sc.getCartKey().equals(key)));
    }

        @Test
        void addProduct_whenTenantContextMismatchesExistingCart_rejectsRequest() {
        String userId = "user1";
        String asin = "B002";
        when(shoppingCartRepository.findProductsInCartByUserId(userId))
            .thenReturn(Optional.of(List.of(buildCart(userId, "B001", 1, "northwind-books"))));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> shoppingCart.addProductToShoppingCart(userId, asin, null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Cart items must all belong to the same tenant context");
        }

    // --- getProductsInCart ---

    @Test
    void getProductsInCart_withItems_returnsAsinToQuantityMap() {
        String userId = "user1";
        List<ShoppingCart> items = Arrays.asList(
                buildCart(userId, "B001", 3),
                buildCart(userId, "B002", 1));
        when(shoppingCartRepository.findProductsInCartByUserId(userId)).thenReturn(Optional.of(items));

        Map<String, Integer> result = shoppingCart.getProductsInCart(userId);

        assertThat(result).hasSize(2).containsEntry("B001", 3).containsEntry("B002", 1);
    }

    @Test
    void getProductsInCart_whenCartIsEmpty_returnsEmptyMap() {
        String userId = "user1";
        when(shoppingCartRepository.findProductsInCartByUserId(userId)).thenReturn(Optional.empty());

        Map<String, Integer> result = shoppingCart.getProductsInCart(userId);

        assertThat(result).isEmpty();
    }

    @Test
    void getCartTenantContext_withTenantOwnedItems_returnsTenantKey() {
        String userId = "user1";
        when(shoppingCartRepository.findProductsInCartByUserId(userId)).thenReturn(Optional.of(List.of(
                buildCart(userId, "B001", 2, "northwind-books"),
                buildCart(userId, "B002", 1, "northwind-books"))));

        CartTenantContext result = shoppingCart.getCartTenantContext(userId);

        assertThat(result.getTenantKey()).isEqualTo("northwind-books");
    }

    // --- removeProductFromCart ---

    @Test
    void removeProduct_whenQuantityGreaterThanOne_decrementsQuantity() {
        String userId = "user1";
        String asin = "B001";
        String key = userId + "-" + asin;
        when(shoppingCartRepository.findById(key)).thenReturn(Optional.of(buildCart(userId, asin, 3)));

        shoppingCart.removeProductFromCart(userId, asin);

        verify(shoppingCartRepository).decrementQuantityForShoppingCart(userId, asin);
        verify(shoppingCartRepository, never()).deleteById(anyString());
    }

    @Test
    void removeProduct_whenQuantityEqualsOne_deletesItem() {
        String userId = "user1";
        String asin = "B001";
        String key = userId + "-" + asin;
        when(shoppingCartRepository.findById(key)).thenReturn(Optional.of(buildCart(userId, asin, 1)));

        shoppingCart.removeProductFromCart(userId, asin);

        verify(shoppingCartRepository, never()).decrementQuantityForShoppingCart(any(), any());
        verify(shoppingCartRepository).deleteById(key);
    }

    @Test
    void removeProduct_whenProductNotInCart_doesNothing() {
        String userId = "user1";
        String asin = "B001";
        String key = userId + "-" + asin;
        when(shoppingCartRepository.findById(key)).thenReturn(Optional.empty());

        shoppingCart.removeProductFromCart(userId, asin);

        verify(shoppingCartRepository, never()).decrementQuantityForShoppingCart(any(), any());
        verify(shoppingCartRepository, never()).deleteById(anyString());
    }

    // --- clearCart ---

    @Test
    void clearCart_whenCartHasItems_deletesAllItemsForUser() {
        String userId = "user1";
        when(shoppingCartRepository.findProductsInCartByUserId(userId))
                .thenReturn(Optional.of(List.of(buildCart(userId, "B001", 1))));

        shoppingCart.clearCart(userId);

        verify(shoppingCartRepository).deleteProductsInCartByUserId(userId);
    }

    @Test
    void clearCart_whenCartIsEmpty_doesNothing() {
        String userId = "user1";
        when(shoppingCartRepository.findProductsInCartByUserId(userId)).thenReturn(Optional.empty());

        shoppingCart.clearCart(userId);

        verify(shoppingCartRepository, never()).deleteProductsInCartByUserId(any());
    }

    // --- helpers ---

    private ShoppingCart buildCart(String userId, String asin, int quantity) {
        return buildCart(userId, asin, quantity, null);
    }

    private ShoppingCart buildCart(String userId, String asin, int quantity, String tenantKey) {
        ShoppingCart cart = new ShoppingCart();
        cart.setCartKey(userId + "-" + asin);
        cart.setUserId(userId);
        cart.setAsin(asin);
        cart.setTenantKey(tenantKey);
        cart.setQuantity(quantity);
        return cart;
    }
}
