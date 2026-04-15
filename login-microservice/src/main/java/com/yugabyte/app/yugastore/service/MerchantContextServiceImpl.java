package com.yugabyte.app.yugastore.service;

import com.yugabyte.app.yugastore.model.MerchantMembership;
import com.yugabyte.app.yugastore.model.MerchantTenant;
import com.yugabyte.app.yugastore.repo.MerchantMembershipRepository;
import com.yugabyte.app.yugastore.repo.MerchantTenantRepository;
import com.yugabyte.app.yugastore.web.MerchantSignupResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MerchantContextServiceImpl implements MerchantContextService {

    private final MerchantMembershipRepository merchantMembershipRepository;
    private final MerchantTenantRepository merchantTenantRepository;

    public MerchantContextServiceImpl(MerchantMembershipRepository merchantMembershipRepository,
            MerchantTenantRepository merchantTenantRepository) {
        this.merchantMembershipRepository = merchantMembershipRepository;
        this.merchantTenantRepository = merchantTenantRepository;
    }

    @Override
    public MerchantSignupResponse getMerchantContext(String authenticatedUserId) {
        List<MerchantSignupResponse> merchantContexts = getMerchantContexts(authenticatedUserId);
        return merchantContexts.get(0);
    }

    @Override
    public List<MerchantSignupResponse> getMerchantContexts(String authenticatedUserId) {
        Long userId = parseAuthenticatedUserId(authenticatedUserId);
        List<MerchantMembership> memberships = merchantMembershipRepository.findAllByUserId(userId);
        if (memberships.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No merchant tenant is linked to this account.");
        }

        List<MerchantSignupResponse> merchantContexts = new ArrayList<>();
        for (MerchantMembership membership : memberships) {
            MerchantTenant tenant = merchantTenantRepository.findById(membership.getMerchantTenantId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Merchant tenant record was not found."));
            merchantContexts.add(MerchantSignupResponse.from(tenant, userId));
        }
        return merchantContexts;
    }

    private Long parseAuthenticatedUserId(String authenticatedUserId) {
        if (authenticatedUserId == null || authenticatedUserId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authenticated user is required.");
        }
        try {
            return Long.valueOf(authenticatedUserId);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authenticated user id is invalid.");
        }
    }
}