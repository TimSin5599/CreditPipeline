package ru.creditbank.apigateway.core.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("User already exists: " + email);
    }
}