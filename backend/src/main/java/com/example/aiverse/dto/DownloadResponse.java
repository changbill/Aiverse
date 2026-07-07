package com.example.aiverse.dto;

import java.time.LocalDateTime;

public record DownloadResponse(String downloadUrl, LocalDateTime expiresAt) {
}
