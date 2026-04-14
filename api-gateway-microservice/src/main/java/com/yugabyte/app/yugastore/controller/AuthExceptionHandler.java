package com.yugabyte.app.yugastore.controller;

import com.yugabyte.app.yugastore.domain.AuthErrorResponse;
import com.yugabyte.app.yugastore.service.AuthProxyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(AuthProxyException.class)
    public ResponseEntity<AuthErrorResponse> handleAuthProxyException(AuthProxyException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getErrorResponse());
    }
}