package com.yugabyte.app.yugastore.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yugabyte.app.yugastore.domain.ProductMetadata;
import com.yugabyte.app.yugastore.domain.ProductRanking;
import com.yugabyte.app.yugastore.domain.ProductRankingKey;
import com.yugabyte.app.yugastore.rest.clients.ProductCatalogRestClient;

@ExtendWith(MockitoExtension.class)
class ProductCatalogServiceRestImplTest {

    @Mock
    private ProductCatalogRestClient productCatalogRestClient;

    private ProductCatalogServiceRestImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductCatalogServiceRestImpl(productCatalogRestClient);
    }

    @Test
    void getProductDetails_delegatesToClient() {
        ProductMetadata expected = buildProduct("B001", "Gadget", 9.99);
        when(productCatalogRestClient.getProductDetails("B001")).thenReturn(expected);

        ProductMetadata result = service.getProductDetails("B001");

        assertThat(result.getId()).isEqualTo("B001");
        assertThat(result.getTitle()).isEqualTo("Gadget");
    }

    @Test
    void getProducts_delegatesToClient() {
        List<ProductMetadata> expected = List.of(buildProduct("B001", "A", 1.0), buildProduct("B002", "B", 2.0));
        when(productCatalogRestClient.getProducts(2, 0)).thenReturn(expected);

        List<ProductMetadata> result = service.getProducts(2, 0);

        assertThat(result).hasSize(2);
    }

    @Test
    void getProductsByCategory_delegatesToClient() {
        ProductRankingKey key = new ProductRankingKey();
        key.setId("B001");
        key.setCategory("Books");
        ProductRanking ranking = new ProductRanking();
        ranking.setId(key);
        ranking.setSalesRank(1);

        when(productCatalogRestClient.getProductsByCategory("Books", 5, 0)).thenReturn(List.of(ranking));

        List<ProductRanking> result = service.getProductsByCategory("Books", 5, 0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSalesRank()).isEqualTo(1);
    }

    @Test
    void getProducts_withOffset_passesParams() {
        when(productCatalogRestClient.getProducts(10, 20)).thenReturn(List.of());

        service.getProducts(10, 20);

        verify(productCatalogRestClient).getProducts(10, 20);
    }

    private ProductMetadata buildProduct(String id, String title, double price) {
        ProductMetadata p = new ProductMetadata();
        p.setId(id);
        p.setTitle(title);
        p.setPrice(price);
        return p;
    }
}
