package com.example.aiverse.dto;

import java.time.LocalDate;

public record DashboardSeriesItemResponse(LocalDate date, long salesCount, long revenueCredit) {
}
