package com.example.aiverse.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetTag;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.LicenseType;

public record AssetDetailResponse(
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
        List<String> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AssetDetailResponse from(Asset asset, List<AssetTag> assetTags) {
        return new AssetDetailResponse(
                asset.getId(), asset.getTitle(), asset.getDescription(), asset.getAssetType(),
                asset.getCategory().getId(), asset.getPreviewObjectKey(), asset.getPriceCredit(),
                asset.getAiTool(), asset.getLicenseType(), asset.getViewCount(),
                asset.getCreator().getId(), asset.getCreator().getNickname(),
                assetTags.stream().map(assetTag -> assetTag.getTag().getName()).toList(),
                asset.getCreatedAt(), asset.getUpdatedAt()
        );
    }
}
