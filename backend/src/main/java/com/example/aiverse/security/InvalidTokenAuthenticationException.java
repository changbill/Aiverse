package com.example.aiverse.security;

import org.springframework.security.core.AuthenticationException;

public class InvalidTokenAuthenticationException extends AuthenticationException {

    public InvalidTokenAuthenticationException(String message) {
        super(message);
    }
}
