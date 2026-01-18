package com.paystream.payment.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CachedBodyFilterTest {

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    private CachedBodyFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CachedBodyFilter();
    }

    @Test
    void doFilterInternal_webhookPath_shouldWrapRequest() throws ServletException, IOException {
        // given
        when(request.getRequestURI()).thenReturn("/payments/webhook");

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(any(CachedBodyHttpServletRequest.class), eq(response));
        verify(filterChain, never()).doFilter(eq(request), eq(response));
    }

    @Test
    void doFilterInternal_nonWebhookPath_shouldPassThrough() throws ServletException, IOException {
        // given
        when(request.getRequestURI()).thenReturn("/payments/ping");

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(eq(request), eq(response));
        verify(filterChain, never()).doFilter(any(CachedBodyHttpServletRequest.class), any());
    }

    @Test
    void doFilterInternal_otherPath_shouldPassThrough() throws ServletException, IOException {
        // given
        when(request.getRequestURI()).thenReturn("/api/other");

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(eq(request), eq(response));
        verify(filterChain, never()).doFilter(any(CachedBodyHttpServletRequest.class), any());
    }
}
