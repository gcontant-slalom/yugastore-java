package com.yugabyte.app.yugastore.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yugabyte.app.yugastore.domain.MerchantSignupResponse;
import com.yugabyte.app.yugastore.service.AuthServiceRest;
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
    private AuthServiceRest authServiceRest;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MerchantContextController(authServiceRest))
                .setControllerAdvice(new AuthExceptionHandler())
                .build();
    }

    @Test
    void currentMerchantContext_returnsTenantContext() throws Exception {
        MerchantSignupResponse response = new MerchantSignupResponse();
        response.setTenantKey("northwind-books");

        when(authServiceRest.currentMerchantContext("42")).thenReturn(response);

        mockMvc.perform(get("/api/v1/merchant-context").header("X-Authenticated-UserId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantKey").value("northwind-books"));
    }

    @Test
    void currentMerchantContexts_returnsTenantList() throws Exception {
        MerchantSignupResponse firstResponse = new MerchantSignupResponse();
        firstResponse.setTenantKey("northwind-books");
        MerchantSignupResponse secondResponse = new MerchantSignupResponse();
        secondResponse.setTenantKey("northwind-music");

        when(authServiceRest.currentMerchantContexts("42")).thenReturn(List.of(firstResponse, secondResponse));

        mockMvc.perform(get("/api/v1/merchant-context/list").header("X-Authenticated-UserId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tenantKey").value("northwind-books"))
                .andExpect(jsonPath("$[1].tenantKey").value("northwind-music"));
    }
}