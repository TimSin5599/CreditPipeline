package ru.creditbank.apigateway.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(CreditRoutingController.class);

    static final String CREDIT_APPLICATION_PATH = "/credit-service/api/v1/credit/";

    private final RestClient creditServiceRestClient;

    public CreditRoutingController(RestClient creditServiceRestClient) {
        this.creditServiceRestClient = creditServiceRestClient;
    }

    @PostMapping(CREDIT_APPLICATION_PATH)
    public ResponseEntity<String> routeCreditApplication(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                           @RequestBody String body) {
        ResponseEntity<String> response = creditServiceRestClient.post()
                .uri(CREDIT_APPLICATION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, resp) -> { })
                .toEntity(String.class);

        log.info("Routed request downstream, method=POST, path={}, status={}",
                CREDIT_APPLICATION_PATH, response.getStatusCode().value());
        return response;
    }
}
