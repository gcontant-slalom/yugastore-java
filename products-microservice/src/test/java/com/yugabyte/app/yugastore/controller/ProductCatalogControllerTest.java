package com.yugabyte.app.yugastore.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.yugabyte.app.yugastore.domain.ProductMetadata;
import com.yugabyte.app.yugastore.domain.ProductRanking;
import com.yugabyte.app.yugastore.domain.ProductRankingKey;
import com.yugabyte.app.yugastore.service.ProductRankingService;
import com.yugabyte.app.yugastore.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductCatalogControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductRankingService productRankingService;

    @InjectMocks
    private ProductCatalogController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getProductDetails_returnsProductJson() throws Exception {
        ProductMetadata product = buildProduct("B001", "Gadget X", 29.99);
        when(productService.findById("B001")).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products-microservice/product/B001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("B001"))
                .andExpect(jsonPath("$.title").value("Gadget X"))
                .andExpect(jsonPath("$.price").value(29.99));
    }

    @Test
    void getProducts_returnsProductList() throws Exception {
        List<ProductMetadata> products = List.of(
                buildProduct("B001", "Title 1", 5.0),
                buildProduct("B002", "Title 2", 15.0));
        when(productService.findAllProductsPageable(2, 0)).thenReturn(products);

        mockMvc.perform(get("/products-microservice/products")
                        .param("limit", "2")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("B001"))
                .andExpect(jsonPath("$[1].id").value("B002"));
    }

    @Test
    void getProducts_whenEmpty_returnsEmptyArray() throws Exception {
        when(productService.findAllProductsPageable(10, 0)).thenReturn(List.of());

        mockMvc.perform(get("/products-microservice/products")
                        .param("limit", "10")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getProductsByCategory_returnsRankingList() throws Exception {
        List<ProductRanking> rankings = List.of(
                buildRanking("B001", "Electronics", 1),
                buildRanking("B002", "Electronics", 2));
        when(productRankingService.getProductsByCategory("Electronics", 2, 0)).thenReturn(rankings);

        mockMvc.perform(get("/products-microservice/products/category/Electronics")
                        .param("limit", "2")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].salesRank").value(1))
                .andExpect(jsonPath("$[1].salesRank").value(2));
    }

    @Test
    void getProductsByCategory_whenNoneFound_returnsEmptyArray() throws Exception {
        when(productRankingService.getProductsByCategory("Unknown", 5, 0)).thenReturn(List.of());

        mockMvc.perform(get("/products-microservice/products/category/Unknown")
                        .param("limit", "5")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private ProductMetadata buildProduct(String id, String title, double price) {
        ProductMetadata p = new ProductMetadata();
        p.setId(id);
        p.setTitle(title);
        p.setPrice(price);
        return p;
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
