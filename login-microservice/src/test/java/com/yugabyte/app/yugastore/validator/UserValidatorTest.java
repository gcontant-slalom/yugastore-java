package com.yugabyte.app.yugastore.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import com.yugabyte.app.yugastore.model.User;
import com.yugabyte.app.yugastore.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserValidator userValidator;

    @Test
    void supports_returnsTrueForUserClass() {
        assertThat(userValidator.supports(User.class)).isTrue();
    }

    @Test
    void supports_returnsFalseForOtherClass() {
        assertThat(userValidator.supports(String.class)).isFalse();
    }

    @Test
    void validate_withValidUser_hasNoErrors() {
        User user = buildUser("johndoe@example.com", "password123", "password123");
        when(userService.findByEmail("johndoe@example.com")).thenReturn(null);

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void validate_withEmptyEmail_rejectsField() {
        User user = buildUser("", "password123", "password123");

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertThat(errors.getFieldError("email")).isNotNull();
    }

    @Test
    void validate_withInvalidEmail_rejectsField() {
        User user = buildUser("abc", "password123", "password123");
        when(userService.findByEmail("abc")).thenReturn(null);

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertThat(errors.getFieldError("email")).isNotNull();
        assertThat(errors.getFieldError("email").getCode()).isEqualTo("Format.userForm.email");
    }

    @Test
    void validate_withDuplicateEmail_rejectsField() {
        User existing = new User();
        existing.setEmail("johndoe@example.com");
        User user = buildUser("johndoe@example.com", "password123", "password123");
        when(userService.findByEmail("johndoe@example.com")).thenReturn(existing);

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertThat(errors.getFieldError("email")).isNotNull();
        assertThat(errors.getFieldError("email").getCode()).isEqualTo("Duplicate.userForm.email");
    }

    @Test
    void validate_withShortPassword_rejectsField() {
        User user = buildUser("johndoe@example.com", "short", "short");
        when(userService.findByEmail("johndoe@example.com")).thenReturn(null);

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertThat(errors.getFieldError("password")).isNotNull();
        assertThat(errors.getFieldError("password").getCode()).isEqualTo("Size.userForm.password");
    }

    @Test
    void validate_withMismatchedPasswords_rejectsConfirmField() {
        User user = buildUser("johndoe@example.com", "password123", "differentPass");
        when(userService.findByEmail("johndoe@example.com")).thenReturn(null);

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertThat(errors.getFieldError("passwordConfirm")).isNotNull();
        assertThat(errors.getFieldError("passwordConfirm").getCode())
                .isEqualTo("Diff.userForm.passwordConfirm");
    }

    @Test
    void validate_withEmptyPassword_rejectsField() {
        User user = buildUser("johndoe@example.com", "", "");

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertThat(errors.getFieldError("password")).isNotNull();
    }

    private User buildUser(String email, String password, String passwordConfirm) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setPasswordConfirm(passwordConfirm);
        return user;
    }
}
