package com.example.aiverse.dto;

import java.time.LocalDateTime;

public record UploadResponse(String objectKey, String uploadUrl, LocalDateTime expiresAt) {
}
