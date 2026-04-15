package com.yugabyte.app.yugastore.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yugabyte.app.yugastore.domain.AuthErrorResponse;
import com.yugabyte.app.yugastore.domain.AuthLoginRequest;
import com.yugabyte.app.yugastore.domain.AuthRegistrationRequest;
import com.yugabyte.app.yugastore.domain.AuthUser;
import com.yugabyte.app.yugastore.domain.MerchantSignupRequest;
import com.yugabyte.app.yugastore.domain.MerchantSignupResponse;
import com.yugabyte.app.yugastore.rest.clients.AuthRestClient;
import com.yugabyte.app.yugastore.service.AuthProxyException;
import com.yugabyte.app.yugastore.service.AuthServiceRest;
import feign.FeignException;
import java.io.IOException;
import java.util.List;
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
    public MerchantSignupResponse createMerchantSignup(MerchantSignupRequest request) {
        AuthUser authenticatedUser = resolveAuthenticatedUser(request);
        MerchantSignupRequest downstreamRequest = new MerchantSignupRequest();
        downstreamRequest.setCompanyName(request.getCompanyName());
        downstreamRequest.setTenantKey(request.getTenantKey());
        downstreamRequest.setAuthenticatedUserId(authenticatedUser.getUserId());
        downstreamRequest.setAuthenticatedUserEmail(authenticatedUser.getEmail());

        try {
            return authRestClient.createMerchantSignup(downstreamRequest);
        } catch (FeignException ex) {
            throw translateException(ex, HttpStatus.BAD_REQUEST, "Merchant signup request rejected.");
        }
    }

    @Override
    public MerchantSignupResponse currentMerchantContext() {
        return currentMerchantContext(null);
    }

    @Override
    public MerchantSignupResponse currentMerchantContext(String authenticatedUserId) {
        AuthUser authenticatedUser = resolveAuthenticatedUser(authenticatedUserId);
        try {
            return authRestClient.getMerchantContext(authenticatedUser.getUserId());
        } catch (FeignException ex) {
            throw translateException(ex, HttpStatus.NOT_FOUND, "No merchant tenant is linked to this account.");
        }
    }

    @Override
    public List<MerchantSignupResponse> currentMerchantContexts() {
        return currentMerchantContexts(null);
    }

    @Override
    public List<MerchantSignupResponse> currentMerchantContexts(String authenticatedUserId) {
        AuthUser authenticatedUser = resolveAuthenticatedUser(authenticatedUserId);
        try {
            return authRestClient.getMerchantContexts(authenticatedUser.getUserId());
        } catch (FeignException ex) {
            throw translateException(ex, HttpStatus.NOT_FOUND, "No merchant tenant is linked to this account.");
        }
    }

    @Override
    public void logout() {
        currentUser = null;
    }

    private AuthUser resolveAuthenticatedUser(MerchantSignupRequest request) {
        if (request.getAuthenticatedUserId() != null && !request.getAuthenticatedUserId().isBlank()) {
            AuthUser authenticatedUser = new AuthUser();
            authenticatedUser.setUserId(request.getAuthenticatedUserId());
            authenticatedUser.setEmail(request.getAuthenticatedUserEmail());
            return authenticatedUser;
        }
        return currentUser();
    }

    private AuthUser resolveAuthenticatedUser(String authenticatedUserId) {
        if (authenticatedUserId != null && !authenticatedUserId.isBlank()) {
            AuthUser authenticatedUser = new AuthUser();
            authenticatedUser.setUserId(authenticatedUserId);
            return authenticatedUser;
        }
        return currentUser();
    }

    private AuthProxyException translateException(FeignException ex, HttpStatus fallbackStatus,
            String fallbackMessage) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        HttpStatus responseStatus = status != null ? status : fallbackStatus;
        AuthErrorResponse errorResponse = new AuthErrorResponse();
        errorResponse.setMessage(fallbackMessage);

        if (ex.contentUTF8() != null && !ex.contentUTF8().isBlank()) {
            try {
                AuthErrorResponse response = objectMapper.readValue(ex.contentUTF8(), AuthErrorResponse.class);
                if (response.getMessage() != null && !response.getMessage().isBlank()) {
                    errorResponse.setMessage(response.getMessage());
                }
                if (response.getFieldErrors() != null && !response.getFieldErrors().isEmpty()) {
                    errorResponse.setFieldErrors(response.getFieldErrors());
                }
            } catch (IOException ignored) {
                errorResponse.setMessage(fallbackMessage);
            }
        }
        return new AuthProxyException(responseStatus, errorResponse, ex);
    }
}
