package com.yugabyte.app.yugastore.rest.clients;

import com.yugabyte.app.yugastore.domain.AuthLoginRequest;
import com.yugabyte.app.yugastore.domain.AuthRegistrationRequest;
import com.yugabyte.app.yugastore.domain.AuthUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("login-microservice")
public interface AuthRestClient {

    @PostMapping(value = "/api/v1/auth/register", consumes = "application/json")
    AuthUser register(@RequestBody AuthRegistrationRequest request);

    @PostMapping(value = "/api/v1/auth/login", consumes = "application/json")
    AuthUser login(@RequestBody AuthLoginRequest request);
}