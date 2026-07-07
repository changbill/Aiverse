package com.example.aiverse.scheduler;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.aiverse.repository.AssetRepository;
import com.example.aiverse.storage.ObjectStorageClient;
import com.example.aiverse.storage.StorageObjectSummary;

@ExtendWith(MockitoExtension.class)
class TempObjectCleanupJobTest {

    @Mock
    private ObjectStorageClient objectStorageClient;

    @Mock
    private AssetRepository assetRepository;

    private TempObjectCleanupJob job;

    @BeforeEach
    void setUp() {
        job = new TempObjectCleanupJob(objectStorageClient, assetRepository);
    }

    @Test
    void 등록되지_않고_24시간이_지난_임시_객체는_삭제한다() {
        StorageObjectSummary old = new StorageObjectSummary("tmp/user-1/old.png", LocalDateTime.now().minusHours(25));
        given(objectStorageClient.listObjects("tmp/")).willReturn(List.of(old));
        given(assetRepository.existsByObjectKey("tmp/user-1/old.png")).willReturn(false);

        job.cleanupUnregisteredTempObjects();

        verify(objectStorageClient).deleteObject("tmp/user-1/old.png");
    }

    @Test
    void 등록된_객체는_24시간이_지나도_삭제하지_않는다() {
        StorageObjectSummary registered = new StorageObjectSummary("tmp/user-1/used.png", LocalDateTime.now().minusHours(25));
        given(objectStorageClient.listObjects("tmp/")).willReturn(List.of(registered));
        given(assetRepository.existsByObjectKey("tmp/user-1/used.png")).willReturn(true);

        job.cleanupUnregisteredTempObjects();

        verify(objectStorageClient, never()).deleteObject(eq("tmp/user-1/used.png"));
    }

    @Test
    void 보관_기간이_지나지_않은_객체는_등록_여부와_무관하게_삭제하지_않는다() {
        StorageObjectSummary fresh = new StorageObjectSummary("tmp/user-1/fresh.png", LocalDateTime.now().minusHours(1));
        given(objectStorageClient.listObjects("tmp/")).willReturn(List.of(fresh));

        job.cleanupUnregisteredTempObjects();

        verify(objectStorageClient, never()).deleteObject(eq("tmp/user-1/fresh.png"));
    }
}
