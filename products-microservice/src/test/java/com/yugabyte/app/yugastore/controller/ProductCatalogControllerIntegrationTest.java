package com.yugabyte.app.yugastore.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.yugabyte.app.yugastore.service.ProductRankingService;
import com.yugabyte.app.yugastore.service.ProductService;

@WebMvcTest(ProductCatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductCatalogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductRankingService productRankingService;

    @Test
    void getProductDetails_whenAsinMissing_returns404() throws Exception {
        when(productService.findById("MISSING")).thenReturn(Optional.empty());

        mockMvc.perform(get("/products-microservice/product/MISSING"))
                .andExpect(status().isNotFound());
    }
}
