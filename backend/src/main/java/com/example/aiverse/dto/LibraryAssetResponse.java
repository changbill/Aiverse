package com.example.aiverse.dto;

import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetStatus;
import com.example.aiverse.entity.AssetType;

public record LibraryAssetResponse(Long id, String title, String previewObjectKey, AssetType assetType, boolean deleted) {

    public static LibraryAssetResponse from(Asset asset) {
        return new LibraryAssetResponse(
                asset.getId(), asset.getTitle(), asset.getPreviewObjectKey(), asset.getAssetType(),
                asset.getStatus() == AssetStatus.DELETED
        );
    }
}
