package com.example.aiverse.dto;

import java.util.List;

import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.LicenseType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AssetCreateRequest(
        @NotBlank(message = "title은 필수입니다.")
        @Size(max = 200, message = "title은 200자 이하여야 합니다.")
        String title,

        @Size(max = 2000, message = "description은 2000자 이하여야 합니다.")
        String description,

        @NotNull(message = "assetType은 필수입니다.")
        AssetType assetType,

        @NotNull(message = "categoryId는 필수입니다.")
        Long categoryId,

        String previewObjectKey,

        @NotBlank(message = "originalObjectKey는 필수입니다.")
        String originalObjectKey,

        @NotBlank(message = "originalFilename은 필수입니다.")
        String originalFilename,

        @NotBlank(message = "contentType은 필수입니다.")
        String contentType,

        @PositiveOrZero(message = "fileSize는 0 이상이어야 합니다.")
        long fileSize,

        @Positive(message = "priceCredit은 0보다 커야 합니다.")
        int priceCredit,

        String aiTool,

        @NotNull(message = "licenseType은 필수입니다.")
        LicenseType licenseType,

        List<String> tags
) {
}
