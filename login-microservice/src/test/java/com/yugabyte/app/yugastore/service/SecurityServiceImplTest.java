package com.yugabyte.app.yugastore.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class SecurityServiceImplTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findLoggedInUsername_whenUserDetailsInContext_returnsUsername() {
        SecurityServiceImpl service = new SecurityServiceImpl();
        UserDetails userDetails = new User("johndoe", "pass", List.of());
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, "pass", List.of());
        // details must be a UserDetails instance for the method to return the name
        auth.setDetails(userDetails);
        SecurityContextHolder.getContext().setAuthentication(auth);

        String result = service.findLoggedInUsername();

        assertThat(result).isEqualTo("johndoe");
    }

    @Test
    void findLoggedInUsername_whenDetailsIsNotUserDetails_returnsNull() {
        SecurityServiceImpl service = new SecurityServiceImpl();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("johndoe", "pass");
        auth.setDetails("some-string-detail");
        SecurityContextHolder.getContext().setAuthentication(auth);

        String result = service.findLoggedInUsername();

        assertThat(result).isNull();
    }
}
