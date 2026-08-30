package ru.creditbank.apigateway.registration.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public UserModel register(RegisterRequest request) {
        UserModel user = createUser(request.email(), request.fullname(), request.password(), Role.USER);
        log.info("User registered successfully, userId={}, role={}", user.getId(), user.getRole());
        return user;
    }

    @Transactional
    public UserModel registerBootstrapAdmin(String email, String rawPassword) {
        String normalized = normalize(email);
        return userRepository.findByEmail(normalized)
                .orElseGet(() -> createUser(normalized, new FullNameRequest("Admin", "Admin", null), rawPassword, Role.ADMIN));
    }

    public LoginResult login(LoginRequest request) {
        String email = normalize(request.email());
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed: no user found for the submitted credentials");
                    return new UserNotFoundException(email);
                });
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed: invalid credentials, userId={}", user.getId());
            throw new InvalidCredentialsException();
        }

        LoginResult result = issueTokens(user);
        log.info("User logged in successfully, userId={}", user.getId());
        return result;
    }

    public LoginResult refresh(String refreshToken) {
        RefreshTokenService.RotatedToken rotated = refreshTokenService.rotate(refreshToken);
        UserModel user = userRepository.findByEmail(rotated.email())
                .orElseThrow(() -> {
                    log.warn("Token refresh failed: user for rotated refresh token no longer exists");
                    return new UserNotFoundException(rotated.email());
                });

        LoginResult result = new LoginResult(jwtService.generateToken(user), rotated.newRefreshToken());
        log.info("Token refreshed successfully, userId={}", user.getId());
        return result;
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

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException(normalized);
        }
    }

    private String normalize(String email) {
        return email.toLowerCase(Locale.ROOT);
    }
}
