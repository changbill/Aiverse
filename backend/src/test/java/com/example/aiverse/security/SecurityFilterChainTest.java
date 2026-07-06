package com.example.aiverse.security;

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
    void 클라이언트가_보낸_요청_id가_401_응답에도_그대로_반영된다() throws Exception {
        String requestId = "client-request-id-1234";

        mockMvc.perform(get("/api/auth/me").header("X-Request-Id", requestId))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Request-Id", requestId))
                .andExpect(jsonPath("$.requestId").value(requestId));
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
}
