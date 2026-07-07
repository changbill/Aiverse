package com.example.aiverse.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.CommonErrorCode;

public enum DashboardPeriod {
    SEVEN_DAYS(6),
    THIRTY_DAYS(29),
    ALL(null);

    private final Integer daysBack;

    DashboardPeriod(Integer daysBack) {
        this.daysBack = daysBack;
    }

    public static DashboardPeriod parse(String raw) {
        if (raw == null) {
            return THIRTY_DAYS;
        }
        return switch (raw) {
            case "7D" -> SEVEN_DAYS;
            case "30D" -> THIRTY_DAYS;
            case "ALL" -> ALL;
            default -> throw new ApplicationException(CommonErrorCode.VALIDATION_ERROR);
        };
    }

    public LocalDate startDate(LocalDate today) {
        return daysBack == null ? null : today.minusDays(daysBack);
    }

    public LocalDateTime from(LocalDate today) {
        LocalDate startDate = startDate(today);
        return startDate == null ? null : startDate.atStartOfDay();
    }
}
