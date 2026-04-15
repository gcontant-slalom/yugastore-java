package com.yugabyte.app.yugastore.service;

import com.yugabyte.app.yugastore.web.MerchantSignupRequest;
import com.yugabyte.app.yugastore.web.MerchantSignupResponse;

public interface MerchantSignupService {
    MerchantSignupResponse createMerchantSignup(MerchantSignupRequest request);
}
