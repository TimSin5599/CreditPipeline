package ru.creditbank.apigateway.routes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CreditRoutingConfig {

    @Bean
    public RestClient creditServiceRestClient(RestClient.Builder builder, CreditServiceProperties properties) {
        return builder.baseUrl(properties.baseUrl()).build();
    }
}
