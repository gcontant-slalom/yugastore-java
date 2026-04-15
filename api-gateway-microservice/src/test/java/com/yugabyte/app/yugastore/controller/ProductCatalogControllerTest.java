package com.yugabyte.app.yugastore.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import com.yugabyte.app.yugastore.domain.MerchantSignupResponse;
import com.yugabyte.app.yugastore.service.AuthServiceRest;
import com.yugabyte.app.yugastore.service.ProductCatalogServiceRest;

@ExtendWith(MockitoExtension.class)
class ProductCatalogControllerTest {

    @Mock
    private ProductCatalogServiceRest productCatalogServiceRest;

    @Mock
    private AuthServiceRest authServiceRest;

    @InjectMocks
    private ProductCatalogController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getProductDetails_returns200WithProduct() throws Exception {
        ProductMetadata product = buildProduct("B001", "Gadget X", 29.99);
        when(productCatalogServiceRest.getProductDetails("B001")).thenReturn(product);

        mockMvc.perform(get("/api/v1/product/B001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("B001"))
                .andExpect(jsonPath("$.title").value("Gadget X"))
                .andExpect(jsonPath("$.price").value(29.99));
    }

    @Test
    void getProductDetails_whenMissing_returns404() throws Exception {
        when(productCatalogServiceRest.getProductDetails("MISSING")).thenReturn(null);

        mockMvc.perform(get("/api/v1/product/MISSING"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProducts_returns200WithList() throws Exception {
        List<ProductMetadata> products = List.of(
                buildProduct("B001", "Title 1", 5.0),
                buildProduct("B002", "Title 2", 10.0));
        when(productCatalogServiceRest.getProducts(2, 0)).thenReturn(products);

        mockMvc.perform(get("/api/v1/products")
                        .param("limit", "2")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("B001"))
                .andExpect(jsonPath("$[1].id").value("B002"));
    }

    @Test
    void getProductsByCategory_returns200WithList() throws Exception {
        List<ProductRanking> rankings = List.of(
                buildRanking("B001", "Electronics", 1),
                buildRanking("B002", "Electronics", 3));
        when(productCatalogServiceRest.getProductsByCategory("Electronics", 2, 0)).thenReturn(rankings);

        mockMvc.perform(get("/api/v1/products/category/Electronics")
                        .param("limit", "2")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].salesRank").value(1));
    }

    @Test
    void getProducts_whenEmpty_returnsEmptyArray() throws Exception {
        when(productCatalogServiceRest.getProducts(10, 0)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/products")
                        .param("limit", "10")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

            @Test
            void getTenantProducts_returns200WithTenantResolvedList() throws Exception {
            MerchantSignupResponse tenantContext = new MerchantSignupResponse();
            tenantContext.setTenantKey("northwind-books");
            tenantContext.setCompanyName("Northwind Books");
            when(authServiceRest.merchantContextForTenantKey("northwind-books")).thenReturn(tenantContext);
            when(productCatalogServiceRest.getProducts(2, 0, "northwind-books", "Northwind Books"))
                .thenReturn(List.of(buildProduct("B001", "Title 1", 5.0)));

            mockMvc.perform(get("/api/v1/tenant/northwind-books/products")
                    .param("limit", "2")
                    .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("B001"));
            }

            @Test
            void getTenantProductDetails_returns200WithTenantResolvedProduct() throws Exception {
            MerchantSignupResponse tenantContext = new MerchantSignupResponse();
            tenantContext.setTenantKey("northwind-books");
            tenantContext.setCompanyName("Northwind Books");
            when(authServiceRest.merchantContextForTenantKey("northwind-books")).thenReturn(tenantContext);
            when(productCatalogServiceRest.getProductDetails("B001", "northwind-books", "Northwind Books"))
                .thenReturn(buildProduct("B001", "Gadget X", 29.99));

            mockMvc.perform(get("/api/v1/tenant/northwind-books/product/B001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("B001"));
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
        ProductRanking r = new ProductRanking();
        r.setId(key);
        r.setSalesRank(salesRank);
        return r;
    }
}
