package com.example.aiverse.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

class RefreshTokenCookieSupportTest {

    @Test
    void 쿠키를_HttpOnly_Secure로_설정한다() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        RefreshTokenCookieSupport.addCookie(response, "raw-token", 1209600);

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).contains("refresh_token=raw-token");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Secure");
        assertThat(setCookie).contains("SameSite=None");
        assertThat(setCookie).contains("Max-Age=1209600");
        assertThat(setCookie).contains("Path=/api/auth");
    }

    @Test
    void 쿠키를_제거할때_Max_Age를_0으로_설정한다() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        RefreshTokenCookieSupport.clearCookie(response);

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).contains("refresh_token=");
        assertThat(setCookie).contains("Max-Age=0");
    }
}
