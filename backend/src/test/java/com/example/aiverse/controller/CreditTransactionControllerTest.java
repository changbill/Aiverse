package com.example.aiverse.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.aiverse.common.response.PageInfo;
import com.example.aiverse.common.response.PageResponse;
import com.example.aiverse.dto.CreditTransactionResponse;
import com.example.aiverse.entity.CreditTransactionType;
import com.example.aiverse.service.CreditTransactionService;

@ExtendWith(MockitoExtension.class)
class CreditTransactionControllerTest {

    @Mock
    private CreditTransactionService creditTransactionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CreditTransactionController(creditTransactionService))
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
    void 거래_이력을_페이지로_반환한다() throws Exception {
        var item = new CreditTransactionResponse(
                21L, CreditTransactionType.CHARGE, 1100, 1100, "credit_product:PLUS", LocalDateTime.now()
        );
        given(creditTransactionService.search(eq(5L), isNull(), eq(0), eq(20)))
                .willReturn(new PageResponse<>(List.of(item), new PageInfo(0, 20, 1, 1, false)));
        authenticateAs(5L);

        mockMvc.perform(get("/api/credit-transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("CHARGE"))
                .andExpect(jsonPath("$.data[0].balanceAfter").value(1100))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }
}
