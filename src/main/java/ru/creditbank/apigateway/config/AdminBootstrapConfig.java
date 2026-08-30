package ru.creditbank.apigateway.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.creditbank.apigateway.registration.service.AuthService;

@Configuration
public class AdminBootstrapConfig {
    @Bean
    public CommandLineRunner adminBootstrapRunner(AuthService authService, AdminBootstrapProperties properties) {
        return args -> authService.registerBootstrapAdmin(properties.email(), properties.password());
    }
}
