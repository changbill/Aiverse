package com.example.aiverse.util;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.AuthErrorCode;

public final class BearerTokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    private BearerTokenExtractor() {
    }

    public static String extract(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new ApplicationException(AuthErrorCode.AUTHENTICATION_REQUIRED);
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}
