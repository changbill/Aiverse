package com.example.aiverse.dto;

import java.util.List;

public record DashboardResponse(
        DashboardTotalsResponse totals,
        List<DashboardSeriesItemResponse> series,
        List<DashboardItemResponse> items
) {
}
