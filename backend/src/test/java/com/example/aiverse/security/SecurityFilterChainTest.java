package com.example.aiverse.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.aiverse.support.IntegrationTestSupport;
import com.example.aiverse.util.RefreshTokenCookieSupport;

import jakarta.servlet.http.Cookie;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class SecurityFilterChainTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 회원가입은_인증_없이_호출할_수_있다() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "filter-chain-register@example.com",
                                  "password": "password1234",
                                  "nickname": "필터체인가입"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void 로그인은_인증_없이_호출할_수_있다() throws Exception {
        registerUser("filter-chain-login@example.com", "필터체인로그인");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "filter-chain-login@example.com",
                                  "password": "password1234"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void authorization_헤더가_없으면_401과_AUTHENTICATION_REQUIRED를_반환한다() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void 유효하지_않은_토큰이면_401과_INVALID_TOKEN을_반환한다() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void 유효한_토큰으로_현재_사용자_정보를_조회한다() throws Exception {
        registerUser("filter-chain-me@example.com", "필터체인미");
        String accessToken = login("filter-chain-me@example.com");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("filter-chain-me@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("필터체인미"));
    }

    @Test
    void 인증_없이_파일_업로드를_요청하면_401을_반환한다() throws Exception {
        mockMvc.perform(post("/api/files/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "purpose": "COVER",
                                  "fileName": "cover.png",
                                  "contentType": "image/png",
                                  "fileSize": 1000
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void 인증_없이_콘텐츠_등록을_요청하면_401을_반환한다() throws Exception {
        mockMvc.perform(post("/api/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void 클라이언트가_보낸_요청_id가_401_응답에도_그대로_반영된다() throws Exception {
        String requestId = "client-request-id-1234";

        mockMvc.perform(get("/api/auth/me").header("X-Request-Id", requestId))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Request-Id", requestId))
                .andExpect(jsonPath("$.requestId").value(requestId));
    }

    @Test
    void 로그인하면_refresh_토큰_쿠키가_설정된다() throws Exception {
        registerUser("filter-chain-refresh-cookie@example.com", "필터체인쿠키");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "filter-chain-refresh-cookie@example.com",
                                  "password": "password1234"
                                }
                                """))
                .andReturn();

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie)
                .contains(RefreshTokenCookieSupport.COOKIE_NAME + "=")
                .contains("HttpOnly");
    }

    @Test
    void refresh_토큰_쿠키로_재발급하면_새_access_토큰과_회전된_refresh_토큰을_받는다() throws Exception {
        registerUser("filter-chain-reissue@example.com", "필터체인재발급");
        String refreshToken = loginAndGetRefreshToken("filter-chain-reissue@example.com");

        MvcResult result = mockMvc.perform(post("/api/auth/reissue")
                        .cookie(new Cookie(RefreshTokenCookieSupport.COOKIE_NAME, refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).contains(RefreshTokenCookieSupport.COOKIE_NAME + "=");
    }

    @Test
    void refresh_토큰_쿠키_없이_재발급하면_401과_AUTHENTICATION_REQUIRED를_반환한다() throws Exception {
        mockMvc.perform(post("/api/auth/reissue"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void 회전으로_폐기된_refresh_토큰을_재사용하면_401과_INVALID_TOKEN을_반환한다() throws Exception {
        registerUser("filter-chain-rotation@example.com", "필터체인회전");
        String refreshToken = loginAndGetRefreshToken("filter-chain-rotation@example.com");

        mockMvc.perform(post("/api/auth/reissue")
                .cookie(new Cookie(RefreshTokenCookieSupport.COOKIE_NAME, refreshToken)));

        mockMvc.perform(post("/api/auth/reissue")
                        .cookie(new Cookie(RefreshTokenCookieSupport.COOKIE_NAME, refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void 로그아웃하면_204를_반환하고_그_refresh_토큰은_더_이상_사용할_수_없다() throws Exception {
        registerUser("filter-chain-logout@example.com", "필터체인로그아웃");
        String refreshToken = loginAndGetRefreshToken("filter-chain-logout@example.com");

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie(RefreshTokenCookieSupport.COOKIE_NAME, refreshToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/reissue")
                        .cookie(new Cookie(RefreshTokenCookieSupport.COOKIE_NAME, refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    private void registerUser(String email, String nickname) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "%s",
                          "password": "password1234",
                          "nickname": "%s"
                        }
                        """.formatted(email, nickname)));
    }

    private String login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "password1234"
                                }
                                """.formatted(email)))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.at("/data/accessToken").asString();
    }

    private String loginAndGetRefreshToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "password1234"
                                }
                                """.formatted(email)))
                .andReturn();

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        String prefix = RefreshTokenCookieSupport.COOKIE_NAME + "=";
        String afterPrefix = setCookie.substring(setCookie.indexOf(prefix) + prefix.length());
        return afterPrefix.substring(0, afterPrefix.indexOf(';'));
    }
}
