package com.example.aiverse.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.AuthErrorCode;

class JwtTokenProviderTest {

    private static final String SECRET_KEY = "test-jwt-secret-key-for-unit-test-0123456789";

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET_KEY, 900L);

    @Test
    void 발급한_토큰을_파싱하면_같은_유저_ID를_얻는다() {
        String token = jwtTokenProvider.issueAccessToken(42L);

        Long userId = jwtTokenProvider.parseUserId(token);

        assertThat(userId).isEqualTo(42L);
    }

    @Test
    void 위조된_토큰은_예외가_발생한다() {
        String token = jwtTokenProvider.issueAccessToken(1L);
        String tamperedToken = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");

        assertThatThrownBy(() -> jwtTokenProvider.parseUserId(tamperedToken))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_TOKEN);
    }

    @Test
    void 만료된_토큰은_예외가_발생한다() {
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider(SECRET_KEY, -1L);
        String expiredToken = expiredTokenProvider.issueAccessToken(1L);

        assertThatThrownBy(() -> jwtTokenProvider.parseUserId(expiredToken))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_TOKEN);
    }

    @Test
    void 다른_비밀키로_서명된_토큰은_예외가_발생한다() {
        JwtTokenProvider otherProvider = new JwtTokenProvider("completely-different-secret-key-0123456789", 900L);
        String tokenFromOtherKey = otherProvider.issueAccessToken(1L);

        assertThatThrownBy(() -> jwtTokenProvider.parseUserId(tokenFromOtherKey))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_TOKEN);
    }
}
