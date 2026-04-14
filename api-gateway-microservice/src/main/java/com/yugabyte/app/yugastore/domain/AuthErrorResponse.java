package com.yugabyte.app.yugastore.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public class AuthErrorResponse {
    private String message;
    private Map<String, String> fieldErrors = new LinkedHashMap<>();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}