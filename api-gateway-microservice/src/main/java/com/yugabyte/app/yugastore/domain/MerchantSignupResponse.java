package com.yugabyte.app.yugastore.domain;

public class MerchantSignupResponse {
    private String tenantId;
    private String tenantKey;
    private String companyName;
    private String merchantAdminUserId;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantKey() {
        return tenantKey;
    }

    public void setTenantKey(String tenantKey) {
        this.tenantKey = tenantKey;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getMerchantAdminUserId() {
        return merchantAdminUserId;
    }

    public void setMerchantAdminUserId(String merchantAdminUserId) {
        this.merchantAdminUserId = merchantAdminUserId;
    }
}
