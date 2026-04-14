package com.yugabyte.app.yugastore.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yugabyte.app.yugastore.model.User;
import com.yugabyte.app.yugastore.service.UserService;
import com.yugabyte.app.yugastore.validator.UserValidator;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserValidator userValidator;

    @Mock
    private AuthenticationManager authenticationManager;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(userService, userValidator, authenticationManager))
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void register_returnsCreatedUser() throws Exception {
        doNothing().when(userValidator).validate(any(), any(Errors.class));
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(42L);
            return null;
        }).when(userService).save(any(User.class));

        AuthRegistrationRequest request = new AuthRegistrationRequest();
        request.setEmail("merchant@example.com");
        request.setPassword("password123");
        request.setPasswordConfirm("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("42"))
                .andExpect(jsonPath("$.email").value("merchant@example.com"));
    }

    @Test
    void register_returnsValidationErrors() throws Exception {
        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("email", "Duplicate.userForm.email");
            return null;
        }).when(userValidator).validate(any(), any(Errors.class));

        AuthRegistrationRequest request = new AuthRegistrationRequest();
        request.setEmail("merchant@example.com");
        request.setPassword("password123");
        request.setPasswordConfirm("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Registration request rejected."))
                .andExpect(jsonPath("$.fieldErrors.email").value("Someone already has that email address."));
    }

    @Test
    void register_returnsDuplicateEmailWhenPersistenceConstraintFails() throws Exception {
        doNothing().when(userValidator).validate(any(), any(Errors.class));
        doAnswer(invocation -> {
            throw new DataIntegrityViolationException("duplicate email");
        }).when(userService).save(any(User.class));

        AuthRegistrationRequest request = new AuthRegistrationRequest();
        request.setEmail("merchant@example.com");
        request.setPassword("password123");
        request.setPasswordConfirm("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Registration request rejected."))
                .andExpect(jsonPath("$.fieldErrors.email").value("Someone already has that email address."));
    }

    @Test
    void login_returnsAuthenticatedUser() throws Exception {
        User user = new User();
        user.setId(7L);
        user.setEmail("merchant@example.com");
        when(userService.findByEmail("merchant@example.com")).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthLoginRequest request = new AuthLoginRequest();
        request.setEmail("merchant@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("7"))
                .andExpect(jsonPath("$.email").value("merchant@example.com"));
    }

    @Test
    void login_rejectsInvalidCredentials() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        AuthLoginRequest request = new AuthLoginRequest();
        request.setEmail("merchant@example.com");
        request.setPassword("wrong-password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }
}