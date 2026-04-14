package com.yugabyte.app.yugastore.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yugabyte.app.yugastore.domain.AuthErrorResponse;
import com.yugabyte.app.yugastore.domain.AuthLoginRequest;
import com.yugabyte.app.yugastore.domain.AuthRegistrationRequest;
import com.yugabyte.app.yugastore.domain.AuthUser;
import com.yugabyte.app.yugastore.service.AuthProxyException;
import com.yugabyte.app.yugastore.service.AuthServiceRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthServiceRest authServiceRest;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authServiceRest))
                .setControllerAdvice(new AuthExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void register_returnsCreatedUser() throws Exception {
        AuthRegistrationRequest request = new AuthRegistrationRequest();
        request.setEmail("merchant@example.com");
        request.setPassword("password123");
        request.setPasswordConfirm("password123");

        AuthUser user = new AuthUser();
        user.setUserId("11");
        user.setEmail("merchant@example.com");
        when(authServiceRest.register(any(AuthRegistrationRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("11"))
                .andExpect(jsonPath("$.email").value("merchant@example.com"));
    }

    @Test
    void login_returnsAuthenticatedUser() throws Exception {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setEmail("merchant@example.com");
        request.setPassword("password123");

        AuthUser user = new AuthUser();
        user.setUserId("11");
        user.setEmail("merchant@example.com");
        when(authServiceRest.login(any(AuthLoginRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("11"))
                .andExpect(jsonPath("$.email").value("merchant@example.com"));
    }

    @Test
    void currentUser_returnsSessionUser() throws Exception {
        AuthUser user = new AuthUser();
        user.setUserId("11");
        user.setEmail("merchant@example.com");
        when(authServiceRest.currentUser()).thenReturn(user);

        mockMvc.perform(get("/api/v1/auth/current-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("11"))
                .andExpect(jsonPath("$.email").value("merchant@example.com"));
    }

    @Test
    void logout_returnsNoContent() throws Exception {
        doNothing().when(authServiceRest).logout();

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());
    }

    @Test
    void register_returnsStructuredValidationErrors() throws Exception {
        AuthRegistrationRequest request = new AuthRegistrationRequest();
        request.setEmail("merchant@example.com");
        request.setPassword("test123");
        request.setPasswordConfirm("test123");

        AuthErrorResponse errorResponse = new AuthErrorResponse();
        errorResponse.setMessage("Registration request rejected.");
        errorResponse.getFieldErrors().put("password", "Try one with at least 8 characters.");
        when(authServiceRest.register(any(AuthRegistrationRequest.class)))
                .thenThrow(new AuthProxyException(HttpStatus.BAD_REQUEST, errorResponse, null));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Registration request rejected."))
                .andExpect(jsonPath("$.fieldErrors.password").value("Try one with at least 8 characters."));
    }
}