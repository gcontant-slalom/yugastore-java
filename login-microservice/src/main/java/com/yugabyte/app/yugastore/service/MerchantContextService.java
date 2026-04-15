package com.yugabyte.app.yugastore.service;

import java.util.List;
import com.yugabyte.app.yugastore.web.MerchantSignupResponse;

public interface MerchantContextService {
    MerchantSignupResponse getMerchantContext(String authenticatedUserId);

    List<MerchantSignupResponse> getMerchantContexts(String authenticatedUserId);

    MerchantSignupResponse getMerchantContextForTenantKey(String tenantKey);
}