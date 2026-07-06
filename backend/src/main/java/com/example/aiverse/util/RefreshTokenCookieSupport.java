package com.example.aiverse.util;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.HttpServletResponse;

public final class RefreshTokenCookieSupport {

    public static final String COOKIE_NAME = "refresh_token";

    private RefreshTokenCookieSupport() {
    }

    public static void addCookie(HttpServletResponse response, String rawToken, long maxAgeSeconds) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(rawToken, Duration.ofSeconds(maxAgeSeconds)).toString());
    }

    public static void clearCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString());
    }

    private static ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
    }
}
