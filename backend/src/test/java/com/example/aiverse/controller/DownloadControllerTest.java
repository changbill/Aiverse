package com.example.aiverse.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.GlobalExceptionHandler;
import com.example.aiverse.common.error.PurchaseErrorCode;
import com.example.aiverse.dto.DownloadResponse;
import com.example.aiverse.service.DownloadService;

@ExtendWith(MockitoExtension.class)
class DownloadControllerTest {

    @Mock
    private DownloadService downloadService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new DownloadController(downloadService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
    }

    @Test
    void 다운로드_URL_발급에_성공한다() throws Exception {
        given(downloadService.download(eq(5L), any())).willReturn(
                new DownloadResponse("https://s3.example.com/download", LocalDateTime.now())
        );
        authenticateAs(5L);

        mockMvc.perform(post("/api/downloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assetId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.downloadUrl").value("https://s3.example.com/download"));
    }

    @Test
    void 구매하지_않은_콘텐츠_다운로드_요청은_404를_반환한다() throws Exception {
        willThrow(new ApplicationException(PurchaseErrorCode.PURCHASE_NOT_FOUND))
                .given(downloadService).download(eq(5L), any());
        authenticateAs(5L);

        mockMvc.perform(post("/api/downloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assetId\": 999}"))
                .andExpect(status().isNotFound());
    }
}
