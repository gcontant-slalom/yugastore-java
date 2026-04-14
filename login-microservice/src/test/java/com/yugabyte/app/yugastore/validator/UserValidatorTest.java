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
        User user = buildUser("johndoe", "password123", "password123");
        when(userService.findByUsername("johndoe")).thenReturn(null);

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void validate_withEmptyUsername_rejectsField() {
        User user = buildUser("", "password123", "password123");

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertThat(errors.getFieldError("username")).isNotNull();
    }

    @Test
    void validate_withTooShortUsername_rejectsField() {
        User user = buildUser("abc", "password123", "password123");
        when(userService.findByUsername("abc")).thenReturn(null);

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertThat(errors.getFieldError("username")).isNotNull();
        assertThat(errors.getFieldError("username").getCode()).isEqualTo("Size.userForm.username");
    }

    @Test
    void validate_withDuplicateUsername_rejectsField() {
        User existing = new User();
        existing.setUsername("johndoe");
        User user = buildUser("johndoe", "password123", "password123");
        when(userService.findByUsername("johndoe")).thenReturn(existing);

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertThat(errors.getFieldError("username")).isNotNull();
        assertThat(errors.getFieldError("username").getCode()).isEqualTo("Duplicate.userForm.username");
    }

    @Test
    void validate_withShortPassword_rejectsField() {
        User user = buildUser("johndoe", "short", "short");
        when(userService.findByUsername("johndoe")).thenReturn(null);

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertThat(errors.getFieldError("password")).isNotNull();
        assertThat(errors.getFieldError("password").getCode()).isEqualTo("Size.userForm.password");
    }

    @Test
    void validate_withMismatchedPasswords_rejectsConfirmField() {
        User user = buildUser("johndoe", "password123", "differentPass");
        when(userService.findByUsername("johndoe")).thenReturn(null);

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertThat(errors.getFieldError("passwordConfirm")).isNotNull();
        assertThat(errors.getFieldError("passwordConfirm").getCode())
                .isEqualTo("Diff.userForm.passwordConfirm");
    }

    @Test
    void validate_withEmptyPassword_rejectsField() {
        User user = buildUser("johndoe", "", "");

        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);

        assertThat(errors.getFieldError("password")).isNotNull();
    }

    private User buildUser(String username, String password, String passwordConfirm) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setPasswordConfirm(passwordConfirm);
        return user;
    }
}
