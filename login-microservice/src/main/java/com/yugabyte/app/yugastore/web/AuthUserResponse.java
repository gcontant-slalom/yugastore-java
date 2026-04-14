package com.yugabyte.app.yugastore.web;

import com.yugabyte.app.yugastore.model.User;

public class AuthUserResponse {
    private String userId;
    private String email;

    public static AuthUserResponse fromUser(User user) {
        AuthUserResponse response = new AuthUserResponse();
        if (user.getId() != null) {
            response.setUserId(String.valueOf(user.getId()));
        }
        response.setEmail(user.getEmail());
        return response;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}