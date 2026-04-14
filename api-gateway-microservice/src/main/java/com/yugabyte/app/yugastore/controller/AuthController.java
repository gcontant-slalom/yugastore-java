package com.yugabyte.app.yugastore.controller;

import com.yugabyte.app.yugastore.domain.AuthLoginRequest;
import com.yugabyte.app.yugastore.domain.AuthRegistrationRequest;
import com.yugabyte.app.yugastore.domain.AuthUser;
import com.yugabyte.app.yugastore.service.AuthServiceRest;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthServiceRest authServiceRest;

    public AuthController(AuthServiceRest authServiceRest) {
        this.authServiceRest = authServiceRest;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthUser> register(@RequestBody AuthRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authServiceRest.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthUser> login(@RequestBody AuthLoginRequest request) {
        return ResponseEntity.ok(authServiceRest.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authServiceRest.logout();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/current-user")
    public ResponseEntity<AuthUser> currentUser() {
        return ResponseEntity.ok(authServiceRest.currentUser());
    }
}