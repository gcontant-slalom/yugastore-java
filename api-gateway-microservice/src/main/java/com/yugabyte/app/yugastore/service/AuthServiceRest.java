package com.yugabyte.app.yugastore.service;

import com.yugabyte.app.yugastore.domain.AuthLoginRequest;
import com.yugabyte.app.yugastore.domain.AuthRegistrationRequest;
import com.yugabyte.app.yugastore.domain.AuthUser;
import com.yugabyte.app.yugastore.domain.MerchantSignupRequest;
import com.yugabyte.app.yugastore.domain.MerchantSignupResponse;
import java.util.List;

public interface AuthServiceRest {
    AuthUser register(AuthRegistrationRequest request);

    AuthUser login(AuthLoginRequest request);

    AuthUser currentUser();

    MerchantSignupResponse createMerchantSignup(MerchantSignupRequest request);

    MerchantSignupResponse currentMerchantContext();

    MerchantSignupResponse currentMerchantContext(String authenticatedUserId);

    List<MerchantSignupResponse> currentMerchantContexts();

    List<MerchantSignupResponse> currentMerchantContexts(String authenticatedUserId);

    void logout();
}
