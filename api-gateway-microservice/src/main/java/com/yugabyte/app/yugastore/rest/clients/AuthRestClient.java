package com.yugabyte.app.yugastore.rest.clients;

import com.yugabyte.app.yugastore.domain.AuthLoginRequest;
import com.yugabyte.app.yugastore.domain.AuthRegistrationRequest;
import com.yugabyte.app.yugastore.domain.AuthUser;
import com.yugabyte.app.yugastore.domain.MerchantSignupRequest;
import com.yugabyte.app.yugastore.domain.MerchantSignupResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("login-microservice")
public interface AuthRestClient {

    @PostMapping(value = "/api/v1/auth/register", consumes = "application/json")
    AuthUser register(@RequestBody AuthRegistrationRequest request);

    @PostMapping(value = "/api/v1/auth/login", consumes = "application/json")
    AuthUser login(@RequestBody AuthLoginRequest request);

    @PostMapping(value = "/api/v1/merchant-signup", consumes = "application/json")
    MerchantSignupResponse createMerchantSignup(@RequestBody MerchantSignupRequest request);

    @GetMapping(value = "/api/v1/merchant-context")
    MerchantSignupResponse getMerchantContext(@RequestParam("userId") String userId);

    @GetMapping(value = "/api/v1/merchant-context/list")
    List<MerchantSignupResponse> getMerchantContexts(@RequestParam("userId") String userId);

    @GetMapping(value = "/api/v1/merchant-context/tenant/{tenantKey}")
    MerchantSignupResponse getMerchantContextForTenantKey(@PathVariable("tenantKey") String tenantKey);
}
