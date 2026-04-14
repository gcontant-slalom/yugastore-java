package com.yugabyte.app.yugastore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.yugabyte.app.yugastore.model.Role;
import com.yugabyte.app.yugastore.model.User;
import com.yugabyte.app.yugastore.repo.RoleRepository;
import com.yugabyte.app.yugastore.repo.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    void save_encodesPasswordAndAssignsRoles() {
        Role role = new Role();
        role.setName("ROLE_USER");
        when(roleRepository.findAll()).thenReturn(List.of(role));
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");

        User user = new User();
        user.setUsername("johndoe");
        user.setPassword("secret");

        userService.save(user);

        assertThat(user.getPassword()).isEqualTo("encoded-secret");
        assertThat(user.getRoles()).containsExactly(role);
        verify(userRepository).save(user);
    }

    @Test
    void save_whenNoRoles_savesUserWithEmptyRoleSet() {
        when(roleRepository.findAll()).thenReturn(List.of());
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        User user = new User();
        user.setUsername("alice");
        user.setPassword("password1");

        userService.save(user);

        assertThat(user.getRoles()).isEmpty();
        verify(userRepository).save(user);
    }

    @Test
    void findByUsername_returnsUser_whenExists() {
        User user = new User();
        user.setUsername("johndoe");
        when(userRepository.findByUsername("johndoe")).thenReturn(user);

        User result = userService.findByUsername("johndoe");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("johndoe");
    }

    @Test
    void findByUsername_returnsNull_whenNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(null);

        User result = userService.findByUsername("unknown");

        assertThat(result).isNull();
    }
}
