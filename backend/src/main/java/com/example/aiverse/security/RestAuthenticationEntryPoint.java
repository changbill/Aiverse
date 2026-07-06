package com.example.aiverse.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.aiverse.common.error.AuthErrorCode;
import com.example.aiverse.common.error.ErrorCode;
import com.example.aiverse.common.error.ErrorResponse;
import com.example.aiverse.common.web.RequestIdFilter;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        ErrorCode errorCode = authException instanceof InvalidTokenAuthenticationException
                ? AuthErrorCode.INVALID_TOKEN
                : AuthErrorCode.AUTHENTICATION_REQUIRED;

        String requestId = RequestIdFilter.currentRequestId(request);
        ErrorResponse body = ErrorResponse.of(errorCode, requestId);

        response.setStatus(errorCode.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
