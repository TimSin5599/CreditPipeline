package ru.creditbank.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin.bootstrap")
public record AdminBootstrapProperties(String email, String password) {
}
