package com.example.aiverse.repository;

import java.time.LocalDate;

public record CreatorDailySales(LocalDate date, long salesCount, long revenueCredit) {
}
