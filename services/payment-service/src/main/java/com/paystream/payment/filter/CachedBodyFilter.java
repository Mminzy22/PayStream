package com.paystream.payment.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** 웹훅 엔드포인트의 요청 body를 캐싱하는 필터 */
@Component
@Order(1)
public class CachedBodyFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().equals("/payments/webhook")) {
            CachedBodyHttpServletRequest cachedBodyRequest =
                    new CachedBodyHttpServletRequest(request);
            filterChain.doFilter(cachedBodyRequest, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
