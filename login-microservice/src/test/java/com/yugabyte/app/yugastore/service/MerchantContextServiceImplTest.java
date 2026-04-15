package com.yugabyte.app.yugastore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.yugabyte.app.yugastore.model.MerchantMembership;
import com.yugabyte.app.yugastore.model.MerchantTenant;
import com.yugabyte.app.yugastore.repo.MerchantMembershipRepository;
import com.yugabyte.app.yugastore.repo.MerchantTenantRepository;
import com.yugabyte.app.yugastore.web.MerchantSignupResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MerchantContextServiceImplTest {

    @Mock
    private MerchantMembershipRepository merchantMembershipRepository;

    @Mock
    private MerchantTenantRepository merchantTenantRepository;

    private MerchantContextServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MerchantContextServiceImpl(merchantMembershipRepository, merchantTenantRepository);
    }

    @Test
    void getMerchantContext_returnsPersistedTenantForUser() {
        MerchantMembership membership = new MerchantMembership();
        membership.setUserId(42L);
        membership.setMerchantTenantId(8L);
        MerchantTenant tenant = new MerchantTenant();
        tenant.setId(8L);
        tenant.setDisplayName("Northwind Books");
        tenant.setTenantKey("northwind-books");

        when(merchantMembershipRepository.findAllByUserId(42L)).thenReturn(List.of(membership));
        when(merchantTenantRepository.findById(8L)).thenReturn(Optional.of(tenant));

        assertThat(service.getMerchantContext("42").getTenantKey()).isEqualTo("northwind-books");
    }

    @Test
    void getMerchantContexts_returnsAllPersistedTenantsForUser() {
        MerchantMembership firstMembership = new MerchantMembership();
        firstMembership.setUserId(42L);
        firstMembership.setMerchantTenantId(8L);

        MerchantMembership secondMembership = new MerchantMembership();
        secondMembership.setUserId(42L);
        secondMembership.setMerchantTenantId(9L);

        MerchantTenant firstTenant = new MerchantTenant();
        firstTenant.setId(8L);
        firstTenant.setDisplayName("Northwind Books");
        firstTenant.setTenantKey("northwind-books");

        MerchantTenant secondTenant = new MerchantTenant();
        secondTenant.setId(9L);
        secondTenant.setDisplayName("Northwind Music");
        secondTenant.setTenantKey("northwind-music");

        when(merchantMembershipRepository.findAllByUserId(42L)).thenReturn(List.of(firstMembership, secondMembership));
        when(merchantTenantRepository.findById(8L)).thenReturn(Optional.of(firstTenant));
        when(merchantTenantRepository.findById(9L)).thenReturn(Optional.of(secondTenant));

        assertThat(service.getMerchantContexts("42"))
                .extracting(MerchantSignupResponse::getTenantKey)
                .containsExactly("northwind-books", "northwind-music");
    }

    @Test
    void getMerchantContext_returnsNotFoundWhenUserHasNoTenant() {
        when(merchantMembershipRepository.findAllByUserId(42L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.getMerchantContext("42"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No merchant tenant is linked to this account.");
    }

    @Test
    void getMerchantContextForTenantKey_returnsPersistedTenantForStorefrontPath() {
        MerchantTenant tenant = new MerchantTenant();
        tenant.setId(8L);
        tenant.setDisplayName("Northwind Books");
        tenant.setTenantKey("northwind-books");
        tenant.setCreatedByUserId(42L);

        when(merchantTenantRepository.findByTenantKey("northwind-books")).thenReturn(Optional.of(tenant));

        assertThat(service.getMerchantContextForTenantKey("Northwind-Books").getCompanyName())
                .isEqualTo("Northwind Books");
    }

    @Test
    void getMerchantContextForTenantKey_returnsNotFoundWhenStorefrontPathIsUnknown() {
        when(merchantTenantRepository.findByTenantKey("unknown-store")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMerchantContextForTenantKey("unknown-store"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No merchant tenant matches that storefront path.");
    }
}