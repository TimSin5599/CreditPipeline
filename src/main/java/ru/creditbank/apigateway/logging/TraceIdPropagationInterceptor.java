package ru.creditbank.apigateway.logging;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

public class TraceIdPropagationInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);
        if (StringUtils.hasText(traceId)) {
            request.getHeaders().add(TraceIdFilter.TRACE_ID_HEADER, traceId);
        }
        return execution.execute(request, body);
    }
}
