package com.example.aiverse.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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

import com.example.aiverse.dto.DashboardItemResponse;
import com.example.aiverse.dto.DashboardResponse;
import com.example.aiverse.dto.DashboardSeriesItemResponse;
import com.example.aiverse.dto.DashboardTotalsResponse;
import com.example.aiverse.service.DashboardService;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService))
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
    void 판매_통계를_반환한다() throws Exception {
        given(dashboardService.getSales(eq(5L), eq("7D"))).willReturn(new DashboardResponse(
                new DashboardTotalsResponse(3, 10, 800),
                List.of(new DashboardSeriesItemResponse(LocalDate.now(), 1, 80)),
                List.of(new DashboardItemResponse(1L, "베스트셀러", 7, 560))
        ));
        authenticateAs(5L);

        mockMvc.perform(get("/api/dashboard/sales").param("period", "7D"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totals.assetCount").value(3))
                .andExpect(jsonPath("$.data.totals.totalSales").value(10))
                .andExpect(jsonPath("$.data.series[0].salesCount").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("베스트셀러"));
    }

    @Test
    void period_파라미터가_없으면_기본값_30D를_사용한다() throws Exception {
        given(dashboardService.getSales(eq(5L), eq("30D"))).willReturn(new DashboardResponse(
                new DashboardTotalsResponse(0, 0, 0), List.of(), List.of()
        ));
        authenticateAs(5L);

        mockMvc.perform(get("/api/dashboard/sales"))
                .andExpect(status().isOk());
    }
}
