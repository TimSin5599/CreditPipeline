package ru.creditbank.apigateway.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class LoanRoutingController {
    private static final Logger log = LoggerFactory.getLogger(LoanRoutingController.class);

    static final String LOANS_PATH = "/loan-management-service/api/v1/payment/loans";
    static final String PAYMENT_PATH = "/loan-management-service/api/v1/payment";
    static final String PAYMENT_HISTORY_PATH = "/loan-management-service/api/payment/history";

    private final RestClient loanServiceRestClient;

    public LoanRoutingController(RestClient loanServiceRestClient) {
        this.loanServiceRestClient = loanServiceRestClient;
    }

    @GetMapping(LOANS_PATH)
    public ResponseEntity<String> getUserLoans(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        ResponseEntity<String> response = loanServiceRestClient.get()
                .uri(LOANS_PATH)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, resp) -> { })
                .toEntity(String.class);

        log.info("Routed request downstream, method=GET, path={}, status={}", LOANS_PATH, response.getStatusCode().value());
        return response;
    }

    @PostMapping(PAYMENT_PATH)
    public ResponseEntity<String> createPayment(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                 @RequestBody String body) {
        ResponseEntity<String> response = loanServiceRestClient.post()
                .uri(PAYMENT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, resp) -> { })
                .toEntity(String.class);

        log.info("Routed request downstream, method=POST, path={}, status={}", PAYMENT_PATH, response.getStatusCode().value());
        return response;
    }

    @GetMapping(PAYMENT_HISTORY_PATH)
    public ResponseEntity<String> getPaymentHistory(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                      @RequestParam String loanId) {
        ResponseEntity<String> response = loanServiceRestClient.get()
                .uri(uriBuilder -> uriBuilder.path(PAYMENT_HISTORY_PATH).queryParam("loanId", loanId).build())
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, resp) -> { })
                .toEntity(String.class);

        log.info("Routed request downstream, method=GET, path={}, status={}", PAYMENT_HISTORY_PATH, response.getStatusCode().value());
        return response;
    }
}
