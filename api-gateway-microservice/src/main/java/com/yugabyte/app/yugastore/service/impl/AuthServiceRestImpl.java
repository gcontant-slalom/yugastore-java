package com.yugabyte.app.yugastore.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yugabyte.app.yugastore.domain.AuthErrorResponse;
import com.yugabyte.app.yugastore.domain.AuthLoginRequest;
import com.yugabyte.app.yugastore.domain.AuthRegistrationRequest;
import com.yugabyte.app.yugastore.domain.AuthUser;
import com.yugabyte.app.yugastore.rest.clients.AuthRestClient;
import com.yugabyte.app.yugastore.service.AuthServiceRest;
import feign.FeignException;
import java.io.IOException;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuthServiceRestImpl implements AuthServiceRest {

    private final AuthRestClient authRestClient;
    private final ObjectMapper objectMapper;
    private AuthUser currentUser;

    public AuthServiceRestImpl(AuthRestClient authRestClient, ObjectMapper objectMapper) {
        this.authRestClient = authRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public AuthUser register(AuthRegistrationRequest request) {
        try {
            return authRestClient.register(request);
        } catch (FeignException ex) {
            throw translateException(ex, HttpStatus.BAD_REQUEST, "Registration request rejected.");
        }
    }

    @Override
    public AuthUser login(AuthLoginRequest request) {
        try {
            currentUser = authRestClient.login(request);
            return currentUser;
        } catch (FeignException ex) {
            throw translateException(ex, HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }
    }

    @Override
    public AuthUser currentUser() {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user.");
        }
        return currentUser;
    }

    @Override
    public void logout() {
        currentUser = null;
    }

    private ResponseStatusException translateException(FeignException ex, HttpStatus fallbackStatus,
            String fallbackMessage) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        HttpStatus responseStatus = status != null ? status : fallbackStatus;
        String message = fallbackMessage;
        if (ex.contentUTF8() != null && !ex.contentUTF8().isBlank()) {
            try {
                AuthErrorResponse response = objectMapper.readValue(ex.contentUTF8(), AuthErrorResponse.class);
                if (response.getMessage() != null && !response.getMessage().isBlank()) {
                    message = response.getMessage();
                }
            } catch (IOException ignored) {
                message = fallbackMessage;
            }
        }
        return new ResponseStatusException(responseStatus, message, ex);
    }
}