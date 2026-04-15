package com.yugabyte.app.yugastore.controller;

import com.yugabyte.app.yugastore.domain.MerchantSignupResponse;
import com.yugabyte.app.yugastore.service.AuthServiceRest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchant-context")
public class MerchantContextController {

    private static final String AUTH_USER_ID_HEADER = "X-Authenticated-UserId";

    private final AuthServiceRest authServiceRest;

    public MerchantContextController(AuthServiceRest authServiceRest) {
        this.authServiceRest = authServiceRest;
    }

    @GetMapping
    public ResponseEntity<MerchantSignupResponse> currentMerchantContext(
            @RequestHeader(value = AUTH_USER_ID_HEADER, required = false) String authenticatedUserId) {
        return ResponseEntity.ok(authServiceRest.currentMerchantContext(authenticatedUserId));
    }

    @GetMapping("/list")
    public ResponseEntity<List<MerchantSignupResponse>> currentMerchantContexts(
            @RequestHeader(value = AUTH_USER_ID_HEADER, required = false) String authenticatedUserId) {
        return ResponseEntity.ok(authServiceRest.currentMerchantContexts(authenticatedUserId));
    }
}