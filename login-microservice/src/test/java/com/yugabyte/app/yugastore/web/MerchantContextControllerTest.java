package com.yugabyte.app.yugastore.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yugabyte.app.yugastore.service.MerchantContextService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MerchantContextControllerTest {

    @Mock
    private MerchantContextService merchantContextService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MerchantContextController(merchantContextService)).build();
    }

    @Test
    void getMerchantContext_returnsPersistedTenantContext() throws Exception {
        MerchantSignupResponse response = new MerchantSignupResponse();
        response.setTenantId("8");
        response.setTenantKey("northwind-books");
        response.setCompanyName("Northwind Books");
        response.setMerchantAdminUserId("42");

        when(merchantContextService.getMerchantContext("42")).thenReturn(response);

        mockMvc.perform(get("/api/v1/merchant-context").param("userId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantKey").value("northwind-books"));
    }

    @Test
    void getMerchantContexts_returnsPersistedTenantContexts() throws Exception {
        MerchantSignupResponse firstResponse = new MerchantSignupResponse();
        firstResponse.setTenantId("8");
        firstResponse.setTenantKey("northwind-books");
        firstResponse.setCompanyName("Northwind Books");
        firstResponse.setMerchantAdminUserId("42");

        MerchantSignupResponse secondResponse = new MerchantSignupResponse();
        secondResponse.setTenantId("9");
        secondResponse.setTenantKey("northwind-music");
        secondResponse.setCompanyName("Northwind Music");
        secondResponse.setMerchantAdminUserId("42");

        when(merchantContextService.getMerchantContexts("42")).thenReturn(List.of(firstResponse, secondResponse));

        mockMvc.perform(get("/api/v1/merchant-context/list").param("userId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tenantKey").value("northwind-books"))
                .andExpect(jsonPath("$[1].tenantKey").value("northwind-music"));
    }

    @Test
    void getMerchantContextForTenantKey_returnsPersistedTenantContext() throws Exception {
        MerchantSignupResponse response = new MerchantSignupResponse();
        response.setTenantId("8");
        response.setTenantKey("northwind-books");
        response.setCompanyName("Northwind Books");
        response.setMerchantAdminUserId("42");

        when(merchantContextService.getMerchantContextForTenantKey("northwind-books")).thenReturn(response);

        mockMvc.perform(get("/api/v1/merchant-context/tenant/northwind-books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantKey").value("northwind-books"));
    }
}