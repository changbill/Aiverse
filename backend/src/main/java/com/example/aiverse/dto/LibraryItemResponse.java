package com.example.aiverse.dto;

import java.time.LocalDateTime;

import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.entity.Purchase;

public record LibraryItemResponse(
        Long purchaseId, LibraryAssetResponse asset, int purchasePriceCredit, LicenseType licenseType, LocalDateTime purchasedAt
) {

    public static LibraryItemResponse from(Purchase purchase) {
        return new LibraryItemResponse(
                purchase.getId(), LibraryAssetResponse.from(purchase.getAsset()),
                purchase.getPurchasePriceCredit(), purchase.getLicenseType(), purchase.getPurchasedAt()
        );
    }
}
