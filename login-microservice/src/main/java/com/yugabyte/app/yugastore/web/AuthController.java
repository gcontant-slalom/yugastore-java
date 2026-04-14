package com.yugabyte.app.yugastore.web;

import com.yugabyte.app.yugastore.model.User;
import com.yugabyte.app.yugastore.service.UserService;
import com.yugabyte.app.yugastore.validator.UserValidator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final UserValidator userValidator;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, UserValidator userValidator,
            AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.userValidator = userValidator;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRegistrationRequest request) {
        User user = new User();
        user.setEmail(normalizeEmail(request.getEmail()));
        user.setPassword(request.getPassword());
        user.setPasswordConfirm(request.getPasswordConfirm());

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(buildValidationError(errors));
        }

        userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthUserResponse.fromUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthLoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (email == null || email.isBlank() || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(buildMessageError("Email and password are required."));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildMessageError("Invalid email or password."));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildMessageError("Authentication failed."));
        }

        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildMessageError("Invalid email or password."));
        }

        return ResponseEntity.ok(AuthUserResponse.fromUser(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthErrorResponse buildValidationError(Errors errors) {
        AuthErrorResponse response = new AuthErrorResponse();
        response.setMessage("Registration request rejected.");
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        errors.getFieldErrors().forEach(error -> fieldErrors.put(error.getField(), mapErrorCode(error.getCode())));
        response.setFieldErrors(fieldErrors);
        return response;
    }

    private AuthErrorResponse buildMessageError(String message) {
        AuthErrorResponse response = new AuthErrorResponse();
        response.setMessage(message);
        return response;
    }

    private String mapErrorCode(String code) {
        if ("NotEmpty".equals(code)) {
            return "This field is required.";
        }
        if ("Format.userForm.email".equals(code)) {
            return "Please provide a valid email address.";
        }
        if ("Duplicate.userForm.email".equals(code)) {
            return "Someone already has that email address.";
        }
        if ("Size.userForm.password".equals(code)) {
            return "Try one with at least 8 characters.";
        }
        if ("Diff.userForm.passwordConfirm".equals(code)) {
            return "These passwords don't match.";
        }
        return "Request rejected.";
    }
}