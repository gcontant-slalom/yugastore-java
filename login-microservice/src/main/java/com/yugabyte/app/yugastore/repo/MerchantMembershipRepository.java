package com.yugabyte.app.yugastore.repo;

import com.yugabyte.app.yugastore.model.MerchantMembership;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantMembershipRepository extends JpaRepository<MerchantMembership, Long> {
	Optional<MerchantMembership> findFirstByUserId(Long userId);

	List<MerchantMembership> findAllByUserId(Long userId);
}
