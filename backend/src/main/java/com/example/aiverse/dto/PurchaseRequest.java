package com.example.aiverse.dto;

import jakarta.validation.constraints.NotNull;

public record PurchaseRequest(
        @NotNull(message = "assetId는 필수입니다.")
        Long assetId
) {
}
