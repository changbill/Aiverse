package com.example.aiverse.dto;

import java.util.List;

import com.example.aiverse.entity.LicenseType;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AssetUpdateRequest(
        @Size(max = 200, message = "title은 200자 이하여야 합니다.")
        String title,

        @Size(max = 2000, message = "description은 2000자 이하여야 합니다.")
        String description,

        Long categoryId,

        String previewObjectKey,

        @Positive(message = "priceCredit은 0보다 커야 합니다.")
        Integer priceCredit,

        String aiTool,

        String originalObjectKey,

        String originalFilename,

        String contentType,

        Long fileSize,

        LicenseType licenseType,

        List<String> tags
) {
    public boolean touchesRestrictedFields() {
        return originalObjectKey != null || originalFilename != null
                || contentType != null || fileSize != null || licenseType != null;
    }
}
