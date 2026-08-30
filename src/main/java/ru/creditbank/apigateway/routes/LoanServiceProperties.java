package ru.creditbank.apigateway.routes;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.loan-management")
public record LoanServiceProperties(String baseUrl) {
}
