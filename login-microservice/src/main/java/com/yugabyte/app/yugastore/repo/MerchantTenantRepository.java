package com.yugabyte.app.yugastore.repo;

import com.yugabyte.app.yugastore.model.MerchantTenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantTenantRepository extends JpaRepository<MerchantTenant, Long> {
}
