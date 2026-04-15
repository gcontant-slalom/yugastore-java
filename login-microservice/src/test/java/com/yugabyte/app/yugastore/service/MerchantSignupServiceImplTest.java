package com.yugabyte.app.yugastore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yugabyte.app.yugastore.model.MerchantMembership;
import com.yugabyte.app.yugastore.model.MerchantTenant;
import com.yugabyte.app.yugastore.model.User;
import com.yugabyte.app.yugastore.repo.MerchantMembershipRepository;
import com.yugabyte.app.yugastore.repo.MerchantTenantRepository;
import com.yugabyte.app.yugastore.repo.UserRepository;
import com.yugabyte.app.yugastore.web.MerchantSignupRequest;
import com.yugabyte.app.yugastore.web.MerchantSignupResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MerchantSignupServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MerchantTenantRepository merchantTenantRepository;

    @Mock
    private MerchantMembershipRepository merchantMembershipRepository;

    private MerchantSignupServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MerchantSignupServiceImpl(userRepository, merchantTenantRepository, merchantMembershipRepository);
    }

    @Test
    void createMerchantSignup_persistsTenantAndMembership() {
        User user = new User();
        user.setId(42L);
        user.setEmail("merchant@example.com");
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(merchantTenantRepository.save(any(MerchantTenant.class))).thenAnswer(invocation -> {
            MerchantTenant tenant = invocation.getArgument(0);
            tenant.setId(8L);
            return tenant;
        });

        MerchantSignupRequest request = new MerchantSignupRequest();
        request.setCompanyName("Northwind Books");
        request.setTenantKey("Northwind Books");
        request.setAuthenticatedUserId("42");
        request.setAuthenticatedUserEmail("merchant@example.com");

        MerchantSignupResponse response = service.createMerchantSignup(request);

        assertThat(response.getTenantId()).isEqualTo("8");
        assertThat(response.getTenantKey()).isEqualTo("northwind-books");
        assertThat(response.getMerchantAdminUserId()).isEqualTo("42");

        ArgumentCaptor<MerchantMembership> membershipCaptor = ArgumentCaptor.forClass(MerchantMembership.class);
        verify(merchantMembershipRepository).save(membershipCaptor.capture());
        assertThat(membershipCaptor.getValue().getRoleName()).isEqualTo("MERCHANT_ADMIN");
        assertThat(membershipCaptor.getValue().getUserId()).isEqualTo(42L);
        assertThat(membershipCaptor.getValue().getMerchantTenantId()).isEqualTo(8L);
    }

    @Test
    void createMerchantSignup_rejectsDuplicateTenantKey() {
        User user = new User();
        user.setId(42L);
        user.setEmail("merchant@example.com");
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(merchantTenantRepository.save(any(MerchantTenant.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        MerchantSignupRequest request = new MerchantSignupRequest();
        request.setCompanyName("Northwind Books");
        request.setTenantKey("northwind-books");
        request.setAuthenticatedUserId("42");
        request.setAuthenticatedUserEmail("merchant@example.com");

        assertThatThrownBy(() -> service.createMerchantSignup(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("That tenant key is already in use.");
    }
}
