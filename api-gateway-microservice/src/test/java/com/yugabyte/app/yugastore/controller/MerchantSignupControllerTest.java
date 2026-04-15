package com.yugabyte.app.yugastore.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yugabyte.app.yugastore.domain.MerchantSignupRequest;
import com.yugabyte.app.yugastore.domain.MerchantSignupResponse;
import com.yugabyte.app.yugastore.service.AuthServiceRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MerchantSignupControllerTest {

    @Mock
    private AuthServiceRest authServiceRest;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MerchantSignupController(authServiceRest))
                .setControllerAdvice(new AuthExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createMerchantSignup_returnsCreatedTenantContext() throws Exception {
        MerchantSignupRequest request = new MerchantSignupRequest();
        request.setCompanyName("Northwind Books");
        request.setTenantKey("northwind-books");

        MerchantSignupResponse response = new MerchantSignupResponse();
        response.setTenantId("7");
        response.setTenantKey("northwind-books");
        response.setCompanyName("Northwind Books");
        response.setMerchantAdminUserId("42");

        when(authServiceRest.createMerchantSignup(any(MerchantSignupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/merchant-signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value("7"))
                .andExpect(jsonPath("$.tenantKey").value("northwind-books"))
                .andExpect(jsonPath("$.merchantAdminUserId").value("42"));
    }
}
