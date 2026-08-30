package ru.creditbank.apigateway.jwt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ru.creditbank.apigateway.config.JwtConfig;
import ru.creditbank.apigateway.core.exception.InvalidRefreshTokenException;

class RefreshTokenServiceTest {
    private static final JwtConfig LONG_LIVED_CONFIG =
            new JwtConfig("ThisIsATestOnlySecretKeyWithAtLeast32Bytes!!", 30, 10080);

    private static final JwtConfig ALREADY_EXPIRED_CONFIG =
            new JwtConfig("ThisIsATestOnlySecretKeyWithAtLeast32Bytes!!", 30, -1);

    @Test
    void issue_thenRotate_returnsNewTokenForSameEmail() {
        RefreshTokenService service = new RefreshTokenService(LONG_LIVED_CONFIG);
        String token = service.issue("test@ya.ru");

        RefreshTokenService.RotatedToken rotated = service.rotate(token);

        assertEquals("test@ya.ru", rotated.email());
        assertNotEquals(token, rotated.newRefreshToken());
    }

    @Test
    void rotate_isSingleUse_secondCallWithSameTokenFails() {
        RefreshTokenService service = new RefreshTokenService(LONG_LIVED_CONFIG);
        String token = service.issue("test@ya.ru");

        service.rotate(token);

        assertThrows(InvalidRefreshTokenException.class, () -> service.rotate(token));
    }

    @Test
    void rotate_unknownToken_throws() {
        RefreshTokenService service = new RefreshTokenService(LONG_LIVED_CONFIG);

        assertThrows(InvalidRefreshTokenException.class, () -> service.rotate("never-issued"));
    }

    @Test
    void rotate_expiredToken_throws() {
        RefreshTokenService service = new RefreshTokenService(ALREADY_EXPIRED_CONFIG);
        String token = service.issue("test@ya.ru");

        assertThrows(InvalidRefreshTokenException.class, () -> service.rotate(token));
    }

    @Test
    void revoke_removesToken() {
        RefreshTokenService service = new RefreshTokenService(LONG_LIVED_CONFIG);
        String token = service.issue("test@ya.ru");

        service.revoke(token);

        assertThrows(InvalidRefreshTokenException.class, () -> service.rotate(token));
    }
}
