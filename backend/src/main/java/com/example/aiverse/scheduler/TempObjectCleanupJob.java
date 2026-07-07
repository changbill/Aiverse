package com.example.aiverse.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.aiverse.repository.AssetRepository;
import com.example.aiverse.storage.ObjectStorageClient;
import com.example.aiverse.storage.StorageObjectSummary;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TempObjectCleanupJob {

    private static final String TEMP_PREFIX = "tmp/";
    private static final Duration RETENTION = Duration.ofHours(24);

    private final ObjectStorageClient objectStorageClient;
    private final AssetRepository assetRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupUnregisteredTempObjects() {
        LocalDateTime cutoff = LocalDateTime.now().minus(RETENTION);
        for (StorageObjectSummary summary : objectStorageClient.listObjects(TEMP_PREFIX)) {
            if (summary.lastModifiedAt().isBefore(cutoff) && !assetRepository.existsByObjectKey(summary.key())) {
                objectStorageClient.deleteObject(summary.key());
            }
        }
    }
}
