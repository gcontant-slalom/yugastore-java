package com.yugabyte.app.yugastore.controller;

import com.yugabyte.app.yugastore.domain.MerchantSignupRequest;
import com.yugabyte.app.yugastore.domain.MerchantSignupResponse;
import com.yugabyte.app.yugastore.service.AuthServiceRest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchant-signup")
public class MerchantSignupController {
    private final AuthServiceRest authServiceRest;

    public MerchantSignupController(AuthServiceRest authServiceRest) {
        this.authServiceRest = authServiceRest;
    }

    @PostMapping
    public ResponseEntity<MerchantSignupResponse> createMerchantSignup(@RequestBody MerchantSignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authServiceRest.createMerchantSignup(request));
    }
}
