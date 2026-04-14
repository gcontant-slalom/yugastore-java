package com.yugabyte.app.yugastore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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
    void loadUserByUsername_returnsUserDetails_forEmailLookup() {
        User user = new User();
        user.setUsername("johndoe");
        user.setEmail("john@example.com");
        user.setPassword("encoded-password");

        when(userRepository.findByUsernameOrEmail("john@example.com", "john@example.com"))
            .thenReturn(user);

        UserDetails result = userDetailsService.loadUserByUsername("john@example.com");

        assertThat(result.getUsername()).isEqualTo("john@example.com");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
    assertThat(result.getAuthorities()).isEmpty();
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenUserNotFound() {
        when(userRepository.findByUsernameOrEmail("ghost", "ghost")).thenReturn(null);

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("ghost");
    }

    @Test
    void loadUserByUsername_supportsLegacyUsernameLookup() {
        User user = new User();
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setPassword("pass");

        when(userRepository.findByUsernameOrEmail("admin", "admin")).thenReturn(user);

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertThat(result.getUsername()).isEqualTo("admin@example.com");
    }
}
