package ru.creditbank.apigateway.routes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
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

class LoanRoutingControllerTest {
    private static final String BASE_URL = "http://loan-management.test";
    private static final String BEARER_TOKEN = "Bearer stub-jwt-token";

    private MockRestServiceServer mockServer;
    private LoanRoutingController controller;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        controller = new LoanRoutingController(builder.build());
    }

    @Test
    void getUserLoans_forwardsAuthorization_returnsDownstreamResponse() {
        String downstreamResponse = "{\"loans\":[]}";

        mockServer.expect(requestToUriTemplate(BASE_URL + LoanRoutingController.LOANS_PATH))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(downstreamResponse));

        ResponseEntity<String> response = controller.getUserLoans(BEARER_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(downstreamResponse, response.getBody());
        mockServer.verify();
    }

    @Test
    void createPayment_forwardsAuthorizationAndBody_returnsDownstreamResponse() {
        String requestBody = "{\"loanId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"amount\":50000.00,\"paymentType\":\"PARTIAL\"}";
        String downstreamResponse = "{\"paymentId\":\"payment-id\",\"newBalance\":70000.00}";

        mockServer.expect(requestToUriTemplate(BASE_URL + LoanRoutingController.PAYMENT_PATH))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(downstreamResponse));

        ResponseEntity<String> response = controller.createPayment(BEARER_TOKEN, requestBody);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(downstreamResponse, response.getBody());
        mockServer.verify();
    }

    @Test
    void createPayment_downstreamError_isPassedThroughUnchanged() {
        String errorBody = "{\"message\":\"Сумма платежа превышает остаток\"}";

        mockServer.expect(requestToUriTemplate(BASE_URL + LoanRoutingController.PAYMENT_PATH))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorBody));

        ResponseEntity<String> response = controller.createPayment(BEARER_TOKEN, "{}");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorBody, response.getBody());
        mockServer.verify();
    }

    @Test
    void getPaymentHistory_forwardsLoanIdQueryParam_returnsDownstreamResponse() {
        String loanId = "3fa85f64-5717-4562-b3fc-2c963f66afa6";
        String downstreamResponse = "{\"payments\":[]}";

        mockServer.expect(requestToUriTemplate(BASE_URL + LoanRoutingController.PAYMENT_HISTORY_PATH + "?loanId={loanId}", loanId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("loanId", loanId))
                .andExpect(header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(downstreamResponse));

        ResponseEntity<String> response = controller.getPaymentHistory(BEARER_TOKEN, loanId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(downstreamResponse, response.getBody());
        mockServer.verify();
    }
}
