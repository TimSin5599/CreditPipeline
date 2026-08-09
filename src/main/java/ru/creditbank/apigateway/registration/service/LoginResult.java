package ru.creditbank.apigateway.registration.service;

public record LoginResult(String accessToken, String refreshToken) {
}
