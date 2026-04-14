package com.yugabyte.app.yugastore.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yugabyte.app.yugastore.domain.ProductInventory;
import com.yugabyte.app.yugastore.repo.ProductInventoryRepository;

@ExtendWith(MockitoExtension.class)
class ProductInventoryServiceImplTest {

    @Mock
    private ProductInventoryRepository productInventoryRepository;

    private ProductInventoryServiceImpl productInventoryService;

    @BeforeEach
    void setUp() {
        productInventoryService = new ProductInventoryServiceImpl(productInventoryRepository);
    }

    @Test
    void findById_returnsInventory_whenFound() {
        ProductInventory inventory = buildInventory("B001", 50);
        when(productInventoryRepository.findById("B001")).thenReturn(Optional.of(inventory));

        Optional<ProductInventory> result = productInventoryService.findById("B001");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("B001");
        assertThat(result.get().getQuantity()).isEqualTo(50);
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(productInventoryRepository.findById("MISSING")).thenReturn(Optional.empty());

        Optional<ProductInventory> result = productInventoryService.findById("MISSING");

        assertThat(result).isEmpty();
    }

    @Test
    void findById_returnInventoryWithZeroQuantity() {
        ProductInventory inventory = buildInventory("B002", 0);
        when(productInventoryRepository.findById("B002")).thenReturn(Optional.of(inventory));

        Optional<ProductInventory> result = productInventoryService.findById("B002");

        assertThat(result).isPresent();
        assertThat(result.get().getQuantity()).isZero();
    }

    private ProductInventory buildInventory(String id, int quantity) {
        ProductInventory inventory = new ProductInventory();
        inventory.setId(id);
        inventory.setQuantity(quantity);
        return inventory;
    }
}
