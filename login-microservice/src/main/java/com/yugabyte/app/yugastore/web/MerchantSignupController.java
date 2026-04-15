package com.yugabyte.app.yugastore.web;

import com.yugabyte.app.yugastore.service.MerchantSignupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/merchant-signup")
public class MerchantSignupController {
    private final MerchantSignupService merchantSignupService;

    public MerchantSignupController(MerchantSignupService merchantSignupService) {
        this.merchantSignupService = merchantSignupService;
    }

    @PostMapping
    public ResponseEntity<?> createMerchantSignup(@RequestBody MerchantSignupRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(merchantSignupService.createMerchantSignup(request));
        } catch (ResponseStatusException ex) {
            AuthErrorResponse errorResponse = new AuthErrorResponse();
            errorResponse.setMessage(ex.getReason());
            return ResponseEntity.status(ex.getStatus()).body(errorResponse);
        }
    }
}
