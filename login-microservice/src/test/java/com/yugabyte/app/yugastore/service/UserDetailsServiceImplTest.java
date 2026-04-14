package com.yugabyte.app.yugastore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.yugabyte.app.yugastore.model.Role;
import com.yugabyte.app.yugastore.model.User;
import com.yugabyte.app.yugastore.repo.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    void loadUserByUsername_returnsUserDetails_withGrantedAuthorities() {
        Role role = new Role();
        role.setName("ROLE_USER");

        User user = new User();
        user.setUsername("johndoe");
        user.setPassword("encoded-password");
        user.setRoles(Set.of(role));

        when(userRepository.findByUsername("johndoe")).thenReturn(user);

        UserDetails result = userDetailsService.loadUserByUsername("johndoe");

        assertThat(result.getUsername()).isEqualTo("johndoe");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(null);

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("ghost");
    }

    @Test
    void loadUserByUsername_withMultipleRoles_includesAllAuthorities() {
        Role roleUser = new Role();
        roleUser.setName("ROLE_USER");
        Role roleAdmin = new Role();
        roleAdmin.setName("ROLE_ADMIN");

        User user = new User();
        user.setUsername("admin");
        user.setPassword("pass");
        user.setRoles(Set.of(roleUser, roleAdmin));

        when(userRepository.findByUsername("admin")).thenReturn(user);

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertThat(result.getAuthorities()).hasSize(2)
                .extracting(a -> a.getAuthority())
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}
