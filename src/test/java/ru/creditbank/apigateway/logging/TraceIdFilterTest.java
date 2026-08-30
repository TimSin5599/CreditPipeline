package ru.creditbank.apigateway.logging;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class TraceIdFilterTest {
    private static final String SERVICE_NAME = "apigateway";

    private TraceIdFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new TraceIdFilter();
        ReflectionTestUtils.setField(filter, "serviceName", SERVICE_NAME);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void missingTraceIdHeader_generatesOneAndPopulatesMdcDuringChain() throws Exception {
        when(request.getHeader(TraceIdFilter.TRACE_ID_HEADER)).thenReturn(null);

        doAnswer(invocation -> {
            assertNotNull(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY));
            assertNotNull(MDC.get(TraceIdFilter.SPAN_ID_MDC_KEY));
            assertNotNull(MDC.get(TraceIdFilter.SERVICE_MDC_KEY));
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response).setHeader(eq(TraceIdFilter.TRACE_ID_HEADER), any(String.class));
        assertNull(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY));
    }

    @Test
    void existingTraceIdHeader_isEchoedBackUnchanged() throws Exception {
        String incomingTraceId = "some-value";
        when(request.getHeader(TraceIdFilter.TRACE_ID_HEADER)).thenReturn(incomingTraceId);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(TraceIdFilter.TRACE_ID_HEADER, incomingTraceId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void afterFilterReturns_mdcIsCleared() throws Exception {
        when(request.getHeader(TraceIdFilter.TRACE_ID_HEADER)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY));
        assertNull(MDC.get(TraceIdFilter.SPAN_ID_MDC_KEY));
        assertNull(MDC.get(TraceIdFilter.SERVICE_MDC_KEY));
    }
}
