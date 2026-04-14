package com.yugabyte.app.yugastore.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yugabyte.app.yugastore.domain.ProductMetadata;
import com.yugabyte.app.yugastore.repo.ProductMetadataRepo;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductMetadataRepo productRepository;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository);
    }

    @Test
    void findById_returnsProduct_whenFound() {
        ProductMetadata product = buildProduct("B001", "Test Title", 9.99);
        when(productRepository.findById("B001")).thenReturn(Optional.of(product));

        Optional<ProductMetadata> result = productService.findById("B001");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("B001");
        assertThat(result.get().getTitle()).isEqualTo("Test Title");
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(productRepository.findById("MISSING")).thenReturn(Optional.empty());

        Optional<ProductMetadata> result = productService.findById("MISSING");

        assertThat(result).isEmpty();
    }

    @Test
    void findAllProductsPageable_returnsListFromRepo() {
        List<ProductMetadata> products = List.of(
                buildProduct("B001", "Title 1", 5.0),
                buildProduct("B002", "Title 2", 10.0));
        when(productRepository.getProducts(2, 0)).thenReturn(products);

        List<ProductMetadata> result = productService.findAllProductsPageable(2, 0);

        assertThat(result).hasSize(2)
                .extracting(ProductMetadata::getId)
                .containsExactly("B001", "B002");
    }

    @Test
    void findAllProductsPageable_withOffset_delegatesCorrectParams() {
        when(productRepository.getProducts(5, 10)).thenReturn(List.of());

        List<ProductMetadata> result = productService.findAllProductsPageable(5, 10);

        assertThat(result).isEmpty();
    }

    private ProductMetadata buildProduct(String id, String title, double price) {
        ProductMetadata p = new ProductMetadata();
        p.setId(id);
        p.setTitle(title);
        p.setPrice(price);
        return p;
    }
}
