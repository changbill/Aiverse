package com.example.aiverse.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.AuthErrorCode;

class BearerTokenExtractorTest {

    @Test
    void Bearer_접두어를_제거하고_토큰을_반환한다() {
        assertThat(BearerTokenExtractor.extract("Bearer abc.def.ghi")).isEqualTo("abc.def.ghi");
    }

    @Test
    void 헤더가_없으면_인증_필요_예외가_발생한다() {
        assertThatThrownBy(() -> BearerTokenExtractor.extract(null))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(AuthErrorCode.AUTHENTICATION_REQUIRED);
    }

    @Test
    void Bearer_접두어가_없으면_인증_필요_예외가_발생한다() {
        assertThatThrownBy(() -> BearerTokenExtractor.extract("abc.def.ghi"))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(AuthErrorCode.AUTHENTICATION_REQUIRED);
    }
}
