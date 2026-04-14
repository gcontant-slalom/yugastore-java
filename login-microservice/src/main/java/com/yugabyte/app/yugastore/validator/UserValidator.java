package com.yugabyte.app.yugastore.validator;

import com.yugabyte.app.yugastore.model.User;
import com.yugabyte.app.yugastore.service.UserService;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        User user = (User) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "NotEmpty");
        if (user.getEmail() != null && !EMAIL_PATTERN.matcher(user.getEmail().trim()).matches()) {
            errors.rejectValue("email", "Format.userForm.email");
        }
        if (user.getEmail() != null && userService.findByEmail(user.getEmail()) != null) {
            errors.rejectValue("email", "Duplicate.userForm.email");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty");
        if (user.getPassword() != null
                && (user.getPassword().length() < 8 || user.getPassword().length() > 32)) {
            errors.rejectValue("password", "Size.userForm.password");
        }

        if (user.getPassword() != null
                && user.getPasswordConfirm() != null
                && !user.getPasswordConfirm().equals(user.getPassword())) {
            errors.rejectValue("passwordConfirm", "Diff.userForm.passwordConfirm");
        }
    }
}
