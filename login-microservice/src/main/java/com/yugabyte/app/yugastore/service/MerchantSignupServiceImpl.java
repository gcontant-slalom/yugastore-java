package com.yugabyte.app.yugastore.service;

import com.yugabyte.app.yugastore.model.MerchantMembership;
import com.yugabyte.app.yugastore.model.MerchantTenant;
import com.yugabyte.app.yugastore.model.User;
import com.yugabyte.app.yugastore.repo.MerchantMembershipRepository;
import com.yugabyte.app.yugastore.repo.MerchantTenantRepository;
import com.yugabyte.app.yugastore.repo.UserRepository;
import com.yugabyte.app.yugastore.web.MerchantSignupRequest;
import com.yugabyte.app.yugastore.web.MerchantSignupResponse;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MerchantSignupServiceImpl implements MerchantSignupService {
    private static final String MERCHANT_ADMIN_ROLE = "MERCHANT_ADMIN";

    private final UserRepository userRepository;
    private final MerchantTenantRepository merchantTenantRepository;
    private final MerchantMembershipRepository merchantMembershipRepository;

    public MerchantSignupServiceImpl(UserRepository userRepository,
            MerchantTenantRepository merchantTenantRepository,
            MerchantMembershipRepository merchantMembershipRepository) {
        this.userRepository = userRepository;
        this.merchantTenantRepository = merchantTenantRepository;
        this.merchantMembershipRepository = merchantMembershipRepository;
    }

    @Override
    @Transactional
    public MerchantSignupResponse createMerchantSignup(MerchantSignupRequest request) {
        Long authenticatedUserId = parseAuthenticatedUserId(request.getAuthenticatedUserId());
        String authenticatedUserEmail = normalizeEmail(request.getAuthenticatedUserEmail());

        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found."));
        if (authenticatedUserEmail == null || !authenticatedUserEmail.equals(normalizeEmail(user.getEmail()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Authenticated user context did not match the current account.");
        }

        MerchantTenant merchantTenant = new MerchantTenant();
        merchantTenant.setDisplayName(normalizeDisplayName(request.getCompanyName()));
        merchantTenant.setTenantKey(normalizeTenantKey(request.getTenantKey()));
        merchantTenant.setCreatedByUserId(authenticatedUserId);

        try {
            merchantTenant = merchantTenantRepository.save(merchantTenant);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That tenant key is already in use.");
        }

        MerchantMembership merchantMembership = new MerchantMembership();
        merchantMembership.setUserId(authenticatedUserId);
        merchantMembership.setMerchantTenantId(merchantTenant.getId());
        merchantMembership.setRoleName(MERCHANT_ADMIN_ROLE);
        merchantMembershipRepository.save(merchantMembership);

        return MerchantSignupResponse.from(merchantTenant, authenticatedUserId);
    }

    private Long parseAuthenticatedUserId(String authenticatedUserId) {
        if (authenticatedUserId == null || authenticatedUserId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user is required.");
        }
        try {
            return Long.valueOf(authenticatedUserId);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authenticated user id is invalid.");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeDisplayName(String displayName) {
        if (displayName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company or store name is required.");
        }
        String normalized = displayName.trim();
        if (normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company or store name is required.");
        }
        return normalized;
    }

    private String normalizeTenantKey(String tenantKey) {
        if (tenantKey == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant key is required.");
        }
        String normalized = tenantKey.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        if (normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant key is required.");
        }
        return normalized;
    }
}
