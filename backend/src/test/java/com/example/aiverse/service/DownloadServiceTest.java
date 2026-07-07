package com.example.aiverse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.PurchaseErrorCode;
import com.example.aiverse.dto.DownloadRequest;
import com.example.aiverse.dto.DownloadResponse;
import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.Category;
import com.example.aiverse.entity.CategoryName;
import com.example.aiverse.entity.Download;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.entity.Purchase;
import com.example.aiverse.entity.User;
import com.example.aiverse.repository.DownloadRepository;
import com.example.aiverse.repository.PurchaseRepository;
import com.example.aiverse.storage.ObjectStorageClient;

@ExtendWith(MockitoExtension.class)
class DownloadServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private DownloadRepository downloadRepository;

    @Mock
    private ObjectStorageClient objectStorageClient;

    private DownloadService downloadService;

    @BeforeEach
    void setUp() {
        downloadService = new DownloadService(purchaseRepository, downloadRepository, objectStorageClient);
    }

    @Test
    void 구매한_콘텐츠는_다운로드_URL을_발급하고_이력을_남긴다() {
        User creator = User.register("dl-creator@example.com", "encoded-password", "다운창작자");
        Category category = category(1L, CategoryName.NATURE, "nature", 1);
        Asset asset = Asset.register(
                creator, "다운로드 대상", null, AssetType.IMAGE, category,
                null, "original/key.png", "file.png", "image/png",
                1000L, 100, null, LicenseType.PERSONAL
        );
        User buyer = User.register("dl-buyer@example.com", "encoded-password", "다운구매자");
        Purchase purchase = Purchase.of(buyer, asset, 1L, "idem-dl", 100, LicenseType.PERSONAL);
        given(purchaseRepository.findByUserIdAndAssetId(5L, 1L)).willReturn(Optional.of(purchase));
        given(objectStorageClient.generateDownloadUrl(eq("original/key.png"), any(Duration.class)))
                .willReturn("https://s3.example.com/download");

        DownloadResponse response = downloadService.download(5L, new DownloadRequest(1L));

        assertThat(response.downloadUrl()).isEqualTo("https://s3.example.com/download");
        assertThat(response.expiresAt()).isNotNull();
        verify(downloadRepository).save(any(Download.class));
    }

    @Test
    void 구매하지_않은_콘텐츠는_다운로드할_수_없다() {
        given(purchaseRepository.findByUserIdAndAssetId(5L, 999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> downloadService.download(5L, new DownloadRequest(999L)))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(PurchaseErrorCode.PURCHASE_NOT_FOUND);
    }

    private Category category(Long id, CategoryName name, String slug, int displayOrder) {
        try {
            var constructor = Category.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Category category = constructor.newInstance();
            setField(category, "id", id);
            setField(category, "name", name);
            setField(category, "slug", slug);
            setField(category, "displayOrder", displayOrder);
            setField(category, "active", true);
            return category;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = Category.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
