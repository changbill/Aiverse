package com.example.aiverse.dto;

import com.example.aiverse.entity.AssetType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UploadRequest(
        @NotNull(message = "purpose는 필수입니다.")
        UploadPurpose purpose,

        AssetType assetType,

        @NotBlank(message = "fileName은 필수입니다.")
        String fileName,

        @NotBlank(message = "contentType은 필수입니다.")
        String contentType,

        @Positive(message = "fileSize는 0보다 커야 합니다.")
        long fileSize
) {
}
