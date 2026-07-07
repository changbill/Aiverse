package com.example.aiverse.dto;

import java.time.LocalDateTime;

import com.example.aiverse.entity.PaymentMethod;
import com.example.aiverse.entity.PaymentStatus;

public record PaymentResponse(
        Long paymentId,
        Long creditProductId,
        int amount,
        PaymentMethod method,
        PaymentStatus status,
        int grantedCredit,
        int creditBalance,
        LocalDateTime paidAt
) {
}
