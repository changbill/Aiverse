package com.example.aiverse.common.web;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Spring Security의 필터 체인이 HIGHEST_PRECEDENCE + 100으로 등록되므로,
// requestId가 Security 단계의 401 응답에도 반영되려면 이 필터가 그보다 먼저 실행돼야 한다.
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Request-Id";
    public static final String ATTRIBUTE_NAME = RequestIdFilter.class.getName() + ".requestId";
    public static final String MDC_KEY = "requestId";

    private static final int MAX_REQUEST_ID_LENGTH = 128;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = resolveRequestId(request.getHeader(HEADER_NAME));
        request.setAttribute(ATTRIBUTE_NAME, requestId);
        response.setHeader(HEADER_NAME, requestId);

        try (MDC.MDCCloseable ignored = MDC.putCloseable(MDC_KEY, requestId)) {
            filterChain.doFilter(request, response);
        }
    }

    public static String currentRequestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(ATTRIBUTE_NAME);
        return requestId instanceof String value ? value : UUID.randomUUID().toString();
    }

    private String resolveRequestId(String candidate) {
        if (candidate == null || candidate.isBlank() || candidate.length() > MAX_REQUEST_ID_LENGTH) {
            return UUID.randomUUID().toString();
        }

        boolean containsControlCharacter = candidate.chars().anyMatch(Character::isISOControl);
        return containsControlCharacter ? UUID.randomUUID().toString() : candidate;
    }
}
