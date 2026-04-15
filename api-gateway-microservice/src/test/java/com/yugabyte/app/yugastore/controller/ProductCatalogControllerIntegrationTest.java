package com.yugabyte.app.yugastore.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.yugabyte.app.yugastore.rest.clients.ProductCatalogRestClient;
import com.yugabyte.app.yugastore.service.impl.ProductCatalogServiceRestImpl;

import feign.FeignException;
import feign.Request;
import feign.Response;

@WebMvcTest(ProductCatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ProductCatalogServiceRestImpl.class)
class ProductCatalogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductCatalogRestClient productCatalogRestClient;

    @Test
    void getProductDetails_whenDownstreamReturns404_returns404() throws Exception {
        when(productCatalogRestClient.getProductDetails("MISSING")).thenThrow(buildNotFound());

        mockMvc.perform(get("/api/v1/product/MISSING"))
                .andExpect(status().isNotFound());
    }

    private FeignException buildNotFound() {
        Request request = Request.create(Request.HttpMethod.GET,
                "http://products-microservice/products-microservice/product/MISSING",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                null);
        Response response = Response.builder()
                .request(request)
                .status(404)
                .reason("Not Found")
                .headers(Collections.emptyMap())
                .body(new byte[0])
                .build();
        return FeignException.errorStatus("ProductCatalogRestClient#getProductDetails", response);
    }
}
