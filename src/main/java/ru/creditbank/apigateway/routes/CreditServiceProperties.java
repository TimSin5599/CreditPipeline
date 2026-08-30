package ru.creditbank.apigateway.routes;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.credit-operations")
public record CreditServiceProperties(String baseUrl) {
}
