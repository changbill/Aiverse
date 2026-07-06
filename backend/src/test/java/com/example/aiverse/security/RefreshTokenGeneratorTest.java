package com.example.aiverse.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RefreshTokenGeneratorTest {

    private final RefreshTokenGenerator generator = new RefreshTokenGenerator();

    @Test
    void 매번_다른_토큰을_생성한다() {
        String first = generator.generateRawToken();
        String second = generator.generateRawToken();

        assertThat(first).isNotBlank();
        assertThat(second).isNotBlank();
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void 같은_토큰은_같은_해시로_변환된다() {
        String hash1 = generator.hash("same-token");
        String hash2 = generator.hash("same-token");

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void 다른_토큰은_다른_해시로_변환된다() {
        assertThat(generator.hash("token-a")).isNotEqualTo(generator.hash("token-b"));
    }

    @Test
    void 원문_토큰은_해시에_포함되지_않는다() {
        String rawToken = "plain-text-token";

        assertThat(generator.hash(rawToken)).doesNotContain(rawToken);
    }
}
