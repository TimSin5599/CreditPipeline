package ru.creditbank.apigateway.jwt.service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import ru.creditbank.apigateway.config.JwtConfig;
import ru.creditbank.apigateway.core.exception.InvalidRefreshTokenException;

@Service
public class RefreshTokenService {

    private final Map<String, RefreshTokenRecord> tokens = new ConcurrentHashMap<>();
    private final JwtConfig jwtConfig;

    public RefreshTokenService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String issue(String email) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(jwtConfig.refreshExpirationMinutes() * 60);
        tokens.put(token, new RefreshTokenRecord(email, expiresAt));
        return token;
    }

    /**
     * Single-use: the presented token is invalidated whether or not rotation succeeds,
     * so a leaked/replayed refresh token can't be used twice.
     */
    public RotatedToken rotate(String refreshToken) {
        RefreshTokenRecord record = tokens.remove(refreshToken);
        if (record == null || record.expiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }

        String newToken = issue(record.email());
        return new RotatedToken(record.email(), newToken);
    }

    public void revoke(String refreshToken) {
        tokens.remove(refreshToken);
    }

    private record RefreshTokenRecord(String email, Instant expiresAt) {
    }

    public record RotatedToken(String email, String newRefreshToken) {
    }
}
