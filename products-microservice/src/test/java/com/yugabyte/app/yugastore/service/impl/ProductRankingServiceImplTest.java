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

import com.yugabyte.app.yugastore.domain.ProductRanking;
import com.yugabyte.app.yugastore.domain.ProductRankingKey;
import com.yugabyte.app.yugastore.repo.ProductRankingRepository;

@ExtendWith(MockitoExtension.class)
class ProductRankingServiceImplTest {

    @Mock
    private ProductRankingRepository productRankingRepository;

    private ProductRankingServiceImpl productRankingService;

    @BeforeEach
    void setUp() {
        productRankingService = new ProductRankingServiceImpl(productRankingRepository);
    }

    @Test
    void findProductRankingById_returnsRanking_whenFound() {
        ProductRanking ranking = buildRanking("B001", "Electronics", 42);
        when(productRankingRepository.findProductRankingById("B001")).thenReturn(Optional.of(ranking));

        Optional<ProductRanking> result = productRankingService.findProductRankingById("B001");

        assertThat(result).isPresent();
        assertThat(result.get().getSalesRank()).isEqualTo(42);
        assertThat(result.get().getId().getAsin()).isEqualTo("B001");
    }

    @Test
    void findProductRankingById_returnsEmpty_whenNotFound() {
        when(productRankingRepository.findProductRankingById("MISSING")).thenReturn(Optional.empty());

        Optional<ProductRanking> result = productRankingService.findProductRankingById("MISSING");

        assertThat(result).isEmpty();
    }

    @Test
    void getProductsByCategory_returnsMatchingProducts() {
        List<ProductRanking> rankings = List.of(
                buildRanking("B001", "Books", 1),
                buildRanking("B002", "Books", 2));
        when(productRankingRepository.getProductsByCategory("Books", 2, 0)).thenReturn(rankings);

        List<ProductRanking> result = productRankingService.getProductsByCategory("Books", 2, 0);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSalesRank()).isEqualTo(1);
        assertThat(result.get(1).getSalesRank()).isEqualTo(2);
    }

    @Test
    void getProductsByCategory_withOffset_delegatesCorrectParams() {
        when(productRankingRepository.getProductsByCategory("Books", 10, 20)).thenReturn(List.of());

        List<ProductRanking> result = productRankingService.getProductsByCategory("Books", 10, 20);

        assertThat(result).isEmpty();
    }

    @Test
    void getProductsByCategory_whenNoneFound_returnsEmptyList() {
        when(productRankingRepository.getProductsByCategory("Unknown", 5, 0)).thenReturn(List.of());

        List<ProductRanking> result = productRankingService.getProductsByCategory("Unknown", 5, 0);

        assertThat(result).isEmpty();
    }

    private ProductRanking buildRanking(String asin, String category, int salesRank) {
        ProductRankingKey key = new ProductRankingKey();
        key.setId(asin);
        key.setCategory(category);

        ProductRanking ranking = new ProductRanking();
        ranking.setId(key);
        ranking.setSalesRank(salesRank);
        ranking.setTitle("Title for " + asin);
        ranking.setPrice(9.99);
        return ranking;
    }
}
