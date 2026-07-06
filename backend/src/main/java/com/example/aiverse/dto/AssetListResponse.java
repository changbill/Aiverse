package com.example.aiverse.dto;

import java.time.LocalDateTime;

import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.LicenseType;

public record AssetListResponse(
        Long id,
        String title,
        String description,
        AssetType assetType,
        Long categoryId,
        String previewObjectKey,
        int priceCredit,
        String aiTool,
        LicenseType licenseType,
        long viewCount,
        Long creatorId,
        String creatorNickname,
        LocalDateTime createdAt
) {
    public static AssetListResponse from(Asset asset) {
        return new AssetListResponse(
                asset.getId(), asset.getTitle(), asset.getDescription(), asset.getAssetType(),
                asset.getCategory().getId(), asset.getPreviewObjectKey(), asset.getPriceCredit(),
                asset.getAiTool(), asset.getLicenseType(), asset.getViewCount(),
                asset.getCreator().getId(), asset.getCreator().getNickname(), asset.getCreatedAt()
        );
    }
}
