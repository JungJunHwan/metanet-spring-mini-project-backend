package com.dashboard.app.global.exception;

public class UserWithdrawnException extends RuntimeException {
    public UserWithdrawnException(String message) {
        super(message);
    }
}
