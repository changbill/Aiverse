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
import com.example.aiverse.dto.PurchaseResponse;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.service.PurchaseService;

@ExtendWith(MockitoExtension.class)
class PurchaseControllerTest {

    @Mock
    private PurchaseService purchaseService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new PurchaseController(purchaseService))
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
    void 구매_요청에_성공하면_201과_구매_결과를_반환한다() throws Exception {
        given(purchaseService.purchase(eq(2L), any(), eq("idem-1"))).willReturn(new PurchaseResponse(
                55L, 10L, 120, LicenseType.COMMERCIAL, 380, LocalDateTime.now()
        ));
        authenticateAs(2L);

        mockMvc.perform(post("/api/purchases")
                        .header("Idempotency-Key", "idem-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assetId\": 10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.purchaseId").value(55))
                .andExpect(jsonPath("$.data.creditBalance").value(380));
    }

    @Test
    void 본인_콘텐츠_구매_요청은_409를_반환한다() throws Exception {
        willThrow(new ApplicationException(PurchaseErrorCode.SELF_PURCHASE_NOT_ALLOWED))
                .given(purchaseService).purchase(eq(1L), any(), eq("idem-self"));
        authenticateAs(1L);

        mockMvc.perform(post("/api/purchases")
                        .header("Idempotency-Key", "idem-self")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assetId\": 10}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SELF_PURCHASE_NOT_ALLOWED"));
    }

    @Test
    void Idempotency_Key_헤더가_없으면_400을_반환한다() throws Exception {
        authenticateAs(2L);

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assetId\": 10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
