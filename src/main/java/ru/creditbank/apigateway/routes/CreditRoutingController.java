package ru.creditbank.apigateway.routes;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class CreditRoutingController {

    static final String CREDIT_APPLICATION_PATH = "/credit-service/api/v1/credit/";

    private final RestClient creditServiceRestClient;

    public CreditRoutingController(RestClient creditServiceRestClient) {
        this.creditServiceRestClient = creditServiceRestClient;
    }

    @PostMapping(CREDIT_APPLICATION_PATH)
    public ResponseEntity<String> routeCreditApplication(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                           @RequestBody String body) {
        return creditServiceRestClient.post()
                .uri(CREDIT_APPLICATION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> { })
                .toEntity(String.class);
    }
}
