package com.yugabyte.app.yugastore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.yugabyte.app.yugastore.model.User;
import com.yugabyte.app.yugastore.repo.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void save_encodesPasswordAndNormalizesEmail() {
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");

        User user = new User();
        user.setEmail("John.Doe@example.com");
        user.setPassword("secret");

        userService.save(user);

        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(user.getUsername()).isEqualTo("john.doe@example.com");
        assertThat(user.getPassword()).isEqualTo("encoded-secret");
        verify(userRepository).save(user);
    }

    @Test
    void save_preservesExplicitUsername() {
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        User user = new User();
        user.setUsername("merchant-admin");
        user.setEmail("alice@example.com");
        user.setPassword("password1");

        userService.save(user);

        assertThat(user.getUsername()).isEqualTo("merchant-admin");
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

    @Test
    void findByEmail_normalizesInputBeforeLookup() {
        User user = new User();
        user.setEmail("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(user);

        User result = userService.findByEmail(" Alice@Example.com ");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
    }
}
