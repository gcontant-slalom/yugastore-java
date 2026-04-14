package com.yugabyte.app.yugastore.service;

import com.yugabyte.app.yugastore.domain.AuthErrorResponse;
import org.springframework.http.HttpStatus;

public class AuthProxyException extends RuntimeException {

    private final HttpStatus status;
    private final AuthErrorResponse errorResponse;

    public AuthProxyException(HttpStatus status, AuthErrorResponse errorResponse, Throwable cause) {
        super(errorResponse != null ? errorResponse.getMessage() : null, cause);
        this.status = status;
        this.errorResponse = errorResponse;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public AuthErrorResponse getErrorResponse() {
        return errorResponse;
    }
}