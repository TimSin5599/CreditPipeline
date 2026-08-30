package ru.creditbank.apigateway.routes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import ru.creditbank.apigateway.logging.TraceIdPropagationInterceptor;

@Configuration
public class LoanRoutingConfig {
    @Bean
    public RestClient loanServiceRestClient(RestClient.Builder builder, LoanServiceProperties properties) {
        return builder.baseUrl(properties.baseUrl())
                .requestInterceptor(new TraceIdPropagationInterceptor())
                .build();
    }
}
