package com.example.aiverse.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.aiverse.common.response.ApiResponse;
import com.example.aiverse.dto.DashboardResponse;
import com.example.aiverse.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Dashboard")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "창작자 판매 통계")
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/sales")
    public ApiResponse<DashboardResponse> getSales(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "30D") String period
    ) {
        return ApiResponse.of(dashboardService.getSales(userId, period));
    }
}
