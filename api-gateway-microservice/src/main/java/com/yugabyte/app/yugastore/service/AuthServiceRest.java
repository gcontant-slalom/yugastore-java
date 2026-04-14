package com.yugabyte.app.yugastore.service;

import com.yugabyte.app.yugastore.domain.AuthLoginRequest;
import com.yugabyte.app.yugastore.domain.AuthRegistrationRequest;
import com.yugabyte.app.yugastore.domain.AuthUser;

public interface AuthServiceRest {
    AuthUser register(AuthRegistrationRequest request);

    AuthUser login(AuthLoginRequest request);

    AuthUser currentUser();

    void logout();
}