package ru.creditbank.apigateway.routes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class CreditRoutingControllerTest {

    private static final String BASE_URL = "http://credit-operations.test";
    private static final String BEARER_TOKEN = "Bearer stub-jwt-token";

    private MockRestServiceServer mockServer;
    private CreditRoutingController controller;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        controller = new CreditRoutingController(builder.build());
    }

    @Test
    void routeCreditApplication_forwardsAuthorizationAndBody_returnsDownstreamResponse() {
        String requestBody = "{\"requestedAmount\":100000,\"termMonths\":12}";
        String downstreamResponse = "{\"id\":\"credit-id\",\"status\":\"PENDING\"}";

        mockServer.expect(requestTo(BASE_URL + CreditRoutingController.CREDIT_APPLICATION_PATH))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(content().json(requestBody))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(downstreamResponse));

        ResponseEntity<String> response = controller.routeCreditApplication(BEARER_TOKEN, requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(downstreamResponse, response.getBody());
        mockServer.verify();
    }

    @Test
    void routeCreditApplication_downstreamError_isPassedThroughUnchanged() {
        String requestBody = "{\"requestedAmount\":-1}";
        String errorBody = "{\"message\":\"Validation failed\"}";

        mockServer.expect(requestTo(BASE_URL + CreditRoutingController.CREDIT_APPLICATION_PATH))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorBody));

        ResponseEntity<String> response = controller.routeCreditApplication(BEARER_TOKEN, requestBody);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorBody, response.getBody());
        mockServer.verify();
    }
}
