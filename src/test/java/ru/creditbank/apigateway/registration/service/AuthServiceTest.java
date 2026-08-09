package ru.creditbank.apigateway.registration.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import ru.creditbank.apigateway.core.Role;
import ru.creditbank.apigateway.core.UserModel;
import ru.creditbank.apigateway.core.exception.InvalidCredentialsException;
import ru.creditbank.apigateway.core.exception.UserAlreadyExistsException;
import ru.creditbank.apigateway.core.exception.UserNotFoundException;
import ru.creditbank.apigateway.core.repository.UserRepository;
import ru.creditbank.apigateway.jwt.service.JwtService;
import ru.creditbank.apigateway.jwt.service.RefreshTokenService;
import ru.creditbank.apigateway.registration.rest.FullNameRequest;
import ru.creditbank.apigateway.registration.rest.LoginRequest;
import ru.creditbank.apigateway.registration.rest.RegisterRequest;

/**
 * userRepository is mocked but backed by a real HashMap, so register-then-login style
 * flows behave like they would against a real database without needing a Spring context.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private final Map<String, UserModel> backingStore = new HashMap<>();

    private AuthService authService;

    private static final RegisterRequest REGISTER_REQUEST = new RegisterRequest(
            "test@ya.ru",
            new FullNameRequest("Имя", "Фамилия", null),
            "Supersecret1"
    );

    @BeforeEach
    void setUp() {
        backingStore.clear();
        lenient().when(userRepository.findByEmail(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(backingStore.get(invocation.getArgument(0))));
        lenient().when(userRepository.save(any(UserModel.class)))
                .thenAnswer(invocation -> {
                    UserModel user = invocation.getArgument(0);
                    backingStore.put(user.getEmail(), user);
                    return user;
                });
        lenient().when(userRepository.findAll())
                .thenAnswer(invocation -> new ArrayList<>(backingStore.values()));

        authService = new AuthService(userRepository, passwordEncoder, jwtService, refreshTokenService);
    }

    @Test
    void register_savesNewUser() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        UserModel user = authService.register(REGISTER_REQUEST);

        assertEquals("test@ya.ru", user.getEmail());
        assertEquals("hashed", user.getPasswordHash());
    }

    @Test
    void register_duplicateEmail_throws() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        authService.register(REGISTER_REQUEST);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(REGISTER_REQUEST));
    }

    @Test
    void login_unknownEmail_throwsUserNotFound() {
        LoginRequest request = new LoginRequest("nobody@ya.ru", "Supersecret1");

        assertThrows(UserNotFoundException.class, () -> authService.login(request));
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        authService.register(REGISTER_REQUEST);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        LoginRequest request = new LoginRequest("test@ya.ru", "WrongPassword1");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_success_returnsAccessAndRefreshTokens() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        authService.register(REGISTER_REQUEST);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(any(UserModel.class))).thenReturn("stub-access-token");
        when(refreshTokenService.issue("test@ya.ru")).thenReturn("stub-refresh-token");

        LoginRequest request = new LoginRequest("test@ya.ru", "Supersecret1");
        LoginResult result = authService.login(request);

        assertEquals("stub-access-token", result.accessToken());
        assertEquals("stub-refresh-token", result.refreshToken());
    }

    @Test
    void refresh_validToken_returnsNewAccessAndRefreshTokens() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        authService.register(REGISTER_REQUEST);
        when(refreshTokenService.rotate("old-refresh-token"))
                .thenReturn(new RefreshTokenService.RotatedToken("test@ya.ru", "new-refresh-token"));
        when(jwtService.generateToken(any(UserModel.class))).thenReturn("new-access-token");

        LoginResult result = authService.refresh("old-refresh-token");

        assertEquals("new-access-token", result.accessToken());
        assertEquals("new-refresh-token", result.refreshToken());
    }

    @Test
    void refresh_userNoLongerExists_throwsUserNotFound() {
        when(refreshTokenService.rotate("orphan-refresh-token"))
                .thenReturn(new RefreshTokenService.RotatedToken("ghost@ya.ru", "new-refresh-token"));

        assertThrows(UserNotFoundException.class, () -> authService.refresh("orphan-refresh-token"));
    }

    @Test
    void registerBootstrapAdmin_createsAdminUser() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        UserModel admin = authService.registerBootstrapAdmin("admin@ya.ru", "AdminPass1");

        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals("admin@ya.ru", admin.getEmail());
    }

    @Test
    void registerBootstrapAdmin_calledTwice_isIdempotent() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        UserModel first = authService.registerBootstrapAdmin("admin@ya.ru", "AdminPass1");
        UserModel second = authService.registerBootstrapAdmin("admin@ya.ru", "AdminPass1");

        assertEquals(first.getId(), second.getId());
        assertEquals(1, authService.listUsers().size());
    }

    @Test
    void promoteToAdmin_unknownEmail_throws() {
        assertThrows(UserNotFoundException.class, () -> authService.promoteToAdmin("nobody@ya.ru"));
    }

    @Test
    void promoteToAdmin_existingUser_grantsAdminRole() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        authService.register(REGISTER_REQUEST);

        UserModel promoted = authService.promoteToAdmin("test@ya.ru");

        assertEquals(Role.ADMIN, promoted.getRole());
    }
}
