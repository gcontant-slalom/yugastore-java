package com.yugabyte.app.yugastore.web;

import com.yugabyte.app.yugastore.service.MerchantContextService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/merchant-context")
public class MerchantContextController {

    private final MerchantContextService merchantContextService;

    public MerchantContextController(MerchantContextService merchantContextService) {
        this.merchantContextService = merchantContextService;
    }

    @GetMapping
    public ResponseEntity<?> getMerchantContext(@RequestParam("userId") String userId) {
        try {
            return ResponseEntity.ok(merchantContextService.getMerchantContext(userId));
        } catch (ResponseStatusException ex) {
            AuthErrorResponse errorResponse = new AuthErrorResponse();
            errorResponse.setMessage(ex.getReason());
            return ResponseEntity.status(ex.getStatus()).body(errorResponse);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getMerchantContexts(@RequestParam("userId") String userId) {
        try {
            List<MerchantSignupResponse> responses = merchantContextService.getMerchantContexts(userId);
            return ResponseEntity.ok(responses);
        } catch (ResponseStatusException ex) {
            AuthErrorResponse errorResponse = new AuthErrorResponse();
            errorResponse.setMessage(ex.getReason());
            return ResponseEntity.status(ex.getStatus()).body(errorResponse);
        }
    }

    @GetMapping("/tenant/{tenantKey}")
    public ResponseEntity<?> getMerchantContextForTenantKey(@PathVariable("tenantKey") String tenantKey) {
        try {
            return ResponseEntity.ok(merchantContextService.getMerchantContextForTenantKey(tenantKey));
        } catch (ResponseStatusException ex) {
            AuthErrorResponse errorResponse = new AuthErrorResponse();
            errorResponse.setMessage(ex.getReason());
            return ResponseEntity.status(ex.getStatus()).body(errorResponse);
        }
    }
}