package com.example.aiverse.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull(message = "creditProductId는 필수입니다.")
        Long creditProductId
) {
}
