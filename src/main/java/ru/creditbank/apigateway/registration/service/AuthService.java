package ru.creditbank.apigateway.registration.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public UserModel register(RegisterRequest request) {
        return createUser(request.email(), request.fullname(), request.password(), Role.USER);
    }

    /**
     * Idempotent on purpose — safe to call on every startup so the configured
     * bootstrap admin exists without duplicating it or overwriting its password on restart.
     */
    public UserModel registerBootstrapAdmin(String email, String rawPassword) {
        String normalized = normalize(email);
        return userRepository.findByEmail(normalized)
                .orElseGet(() -> createUser(normalized, new FullNameRequest("Admin", "Admin", null), rawPassword, Role.ADMIN));
    }

    public LoginResult login(LoginRequest request) {
        String email = normalize(request.email());
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return issueTokens(user);
    }

    public LoginResult refresh(String refreshToken) {
        RefreshTokenService.RotatedToken rotated = refreshTokenService.rotate(refreshToken);
        UserModel user = userRepository.findByEmail(rotated.email())
                .orElseThrow(() -> new UserNotFoundException(rotated.email()));

        return new LoginResult(jwtService.generateToken(user), rotated.newRefreshToken());
    }

    private LoginResult issueTokens(UserModel user) {
        return new LoginResult(jwtService.generateToken(user), refreshTokenService.issue(user.getEmail()));
    }

    public List<UserModel> listUsers() {
        return userRepository.findAll();
    }

    public UserModel promoteToAdmin(String email) {
        UserModel user = userRepository.findByEmail(normalize(email))
                .orElseThrow(() -> new UserNotFoundException(email));
        user.setRole(Role.ADMIN);
        return userRepository.save(user);
    }

    private UserModel createUser(String email, FullNameRequest fullname, String rawPassword, Role role) {
        String normalized = normalize(email);
        if (userRepository.findByEmail(normalized).isPresent()) {
            throw new UserAlreadyExistsException(normalized);
        }

        UserModel user = UserModel.builder()
                .id(UUID.randomUUID().toString())
                .email(normalized)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .firstname(fullname.firstname())
                .lastname(fullname.lastname())
                .middlename(fullname.middlename())
                .role(role)
                .build();

        return userRepository.save(user);
    }

    private String normalize(String email) {
        return email.toLowerCase(Locale.ROOT);
    }
}
