package com.yugabyte.app.yugastore.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yugabyte.app.yugastore.domain.AuthLoginRequest;
import com.yugabyte.app.yugastore.domain.AuthRegistrationRequest;
import com.yugabyte.app.yugastore.domain.AuthUser;
import com.yugabyte.app.yugastore.rest.clients.AuthRestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceRestImplTest {

    @Mock
    private AuthRestClient authRestClient;

    private AuthServiceRestImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceRestImpl(authRestClient, new ObjectMapper());
    }

    @Test
    void register_delegatesToClient() {
        AuthRegistrationRequest request = new AuthRegistrationRequest();
        request.setEmail("merchant@example.com");
        request.setPassword("password123");
        request.setPasswordConfirm("password123");

        AuthUser user = new AuthUser();
        user.setUserId("12");
        user.setEmail("merchant@example.com");
        when(authRestClient.register(request)).thenReturn(user);

        AuthUser result = service.register(request);

        assertThat(result.getUserId()).isEqualTo("12");
        assertThat(result.getEmail()).isEqualTo("merchant@example.com");
    }

    @Test
    void login_storesCurrentUserInSessionScopedService() {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setEmail("merchant@example.com");
        request.setPassword("password123");

        AuthUser user = new AuthUser();
        user.setUserId("9");
        user.setEmail("merchant@example.com");
        when(authRestClient.login(request)).thenReturn(user);

        AuthUser result = service.login(request);

        assertThat(result.getUserId()).isEqualTo("9");
        assertThat(service.currentUser().getEmail()).isEqualTo("merchant@example.com");
    }

    @Test
    void currentUser_throwsWhenSessionIsUnauthenticated() {
        assertThatThrownBy(() -> service.currentUser())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401 UNAUTHORIZED")
                .hasMessageContaining("No authenticated user.");
    }

    @Test
    void logout_clearsCurrentUser() {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setEmail("merchant@example.com");
        request.setPassword("password123");

        AuthUser user = new AuthUser();
        user.setUserId("9");
        user.setEmail("merchant@example.com");
        when(authRestClient.login(request)).thenReturn(user);

        service.login(request);
        service.logout();

        assertThatThrownBy(() -> service.currentUser())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No authenticated user.");
        verify(authRestClient).login(request);
    }
}