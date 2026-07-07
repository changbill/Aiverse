package com.example.aiverse.storage;

import java.time.LocalDateTime;

public record StorageObjectSummary(String key, LocalDateTime lastModifiedAt) {
}
