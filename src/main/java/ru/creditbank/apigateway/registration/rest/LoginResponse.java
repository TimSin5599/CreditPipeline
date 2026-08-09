package ru.creditbank.apigateway.registration.rest;

public record LoginResponse(String token, String refreshToken) {
}
