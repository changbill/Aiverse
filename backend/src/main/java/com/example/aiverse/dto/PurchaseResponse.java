package com.example.aiverse.dto;

import java.time.LocalDateTime;

import com.example.aiverse.entity.LicenseType;

public record PurchaseResponse(
        Long purchaseId,
        Long assetId,
        int purchasePriceCredit,
        LicenseType licenseType,
        int creditBalance,
        LocalDateTime purchasedAt
) {
}
