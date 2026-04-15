package com.yugabyte.yugastore.ui.controller;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.yugabyte.yugastore.ui.YugastoreFrontend;
import com.yugabyte.yugastore.ui.model.AuthUser;
import com.yugabyte.yugastore.ui.rest.DashboardRestConsumer;

@SpringBootTest(classes = YugastoreFrontend.class)
@AutoConfigureMockMvc(addFilters = false)
class CronosProductsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    }

    @Test
    void getProductDetails_whenGatewayReturns404_returnsEmptyJsonObject() throws Exception {
        server.expect(requestTo("http://localhost:8081/api/v1/product/MISSING"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"not found\"}"));

        mockMvc.perform(get("/products/details").param("asin", "MISSING"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));

        server.verify();
    }

        @Test
        void getTenantProductDetails_whenGatewayReturns404_returnsEmptyJsonObject() throws Exception {
        server.expect(requestTo("http://localhost:8081/api/v1/tenant/northwind-books/product/MISSING"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\":\"not found\"}"));

        mockMvc.perform(get("/tenant/northwind-books/products/details").param("asin", "MISSING"))
            .andExpect(status().isOk())
            .andExpect(content().json("{}"));

        server.verify();
        }

        @Test
        void getMerchantContextForTenantKey_proxiesPublicLookup() throws Exception {
        server.expect(requestTo("http://localhost:8081/api/v1/merchant-context/tenant/northwind-books"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"tenantKey\":\"northwind-books\",\"companyName\":\"Northwind Books\"}"));

        mockMvc.perform(get("/api/v1/merchant-context/tenant/northwind-books"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"tenantKey\":\"northwind-books\",\"companyName\":\"Northwind Books\"}"));

        server.verify();
        }

    @Test
    void checkoutCart_forwardsTenantContextHeadersToGateway() throws Exception {
        AuthUser authUser = new AuthUser();
        authUser.setUserId("42");
        authUser.setEmail("merchant@example.com");

        server.expect(requestTo("http://localhost:8081/api/v1/shoppingCart/checkout"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(DashboardRestConsumer.AUTH_USER_ID_HEADER, "42"))
                .andExpect(header(DashboardRestConsumer.TENANT_KEY_HEADER, "northwind-books"))
                .andExpect(header(DashboardRestConsumer.MERCHANT_COMPANY_NAME_HEADER, "Northwind Books"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"status\":\"SUCCESS\",\"orderNumber\":\"order-abc\"}"));

        mockMvc.perform(post("/cart/checkout")
                        .sessionAttr("authUser", authUser)
                        .header(DashboardRestConsumer.TENANT_KEY_HEADER, "northwind-books")
                        .header(DashboardRestConsumer.MERCHANT_COMPANY_NAME_HEADER, "Northwind Books"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"SUCCESS\",\"orderNumber\":\"order-abc\"}"));

        server.verify();
    }

    @Test
    void getCartTenantContext_proxiesTenantContextLookup() throws Exception {
        AuthUser authUser = new AuthUser();
        authUser.setUserId("42");
        authUser.setEmail("merchant@example.com");

        server.expect(requestTo("http://localhost:8081/api/v1/shoppingCart/tenant-context"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(DashboardRestConsumer.AUTH_USER_ID_HEADER, "42"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"tenantKey\":\"northwind-books\"}"));

        mockMvc.perform(get("/cart/tenant-context")
                        .sessionAttr("authUser", authUser))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"tenantKey\":\"northwind-books\"}"));

        server.verify();
    }
}
