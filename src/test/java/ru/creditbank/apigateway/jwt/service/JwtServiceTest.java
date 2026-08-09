package ru.creditbank.apigateway.jwt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.creditbank.apigateway.config.JwtConfig;
import ru.creditbank.apigateway.core.Role;
import ru.creditbank.apigateway.core.UserModel;

class JwtServiceTest {

    private static final JwtConfig TEST_CONFIG =
            new JwtConfig("ThisIsATestOnlySecretKeyWithAtLeast32Bytes!!", 30, 10080);

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_CONFIG);
    }

    @Test
    void generateToken_isValidAndCarriesEmailAndRole() {
        UserModel user = UserModel.builder().email("test@ya.ru").role(Role.USER).build();

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token));
        assertEquals("test@ya.ru", jwtService.extractEmail(token));
        assertEquals("USER", jwtService.extractRole(token));
    }

    @Test
    void isTokenValid_garbageToken_returnsFalse() {
        assertFalse(jwtService.isTokenValid("not-a-real-jwt"));
    }
}