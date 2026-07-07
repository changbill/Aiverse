package com.example.aiverse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.FileErrorCode;
import com.example.aiverse.dto.UploadPurpose;
import com.example.aiverse.dto.UploadRequest;
import com.example.aiverse.dto.UploadResponse;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.storage.ObjectStorageClient;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

    @Mock
    private ObjectStorageClient objectStorageClient;

    private FileUploadService fileUploadService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        fileUploadService = new FileUploadService(objectStorageClient);
    }

    @Test
    void 형식과_용량이_유효하면_사용자별_임시_객체_키로_업로드_URL을_발급한다() {
        given(objectStorageClient.generateUploadUrl(anyString(), anyString(), any(Duration.class)))
                .willReturn("https://s3.example.com/presigned");

        UploadRequest request = new UploadRequest(UploadPurpose.ORIGINAL, AssetType.IMAGE, "sunset.png", "image/png", 1_000_000L);
        UploadResponse response = fileUploadService.issueUploadUrl(5L, request);

        assertThat(response.uploadUrl()).isEqualTo("https://s3.example.com/presigned");
        assertThat(response.objectKey()).startsWith("tmp/user-5/").endsWith("/sunset.png");
        assertThat(response.expiresAt()).isNotNull();
    }

    @Test
    void 허용되지_않는_형식이면_예외를_던진다() {
        UploadRequest request = new UploadRequest(UploadPurpose.COVER, null, "cover.gif", "image/gif", 1000L);

        assertThatThrownBy(() -> fileUploadService.issueUploadUrl(5L, request))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(FileErrorCode.INVALID_FILE_FORMAT);
    }

    @Test
    void 최대_크기를_초과하면_예외를_던진다() {
        UploadRequest request = new UploadRequest(UploadPurpose.COVER, null, "cover.png", "image/png", 20L * 1024 * 1024);

        assertThatThrownBy(() -> fileUploadService.issueUploadUrl(5L, request))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(FileErrorCode.FILE_TOO_LARGE);
    }

    @Test
    void 원본_업로드는_assetType별_형식과_크기_제한을_따른다() {
        UploadRequest request = new UploadRequest(UploadPurpose.ORIGINAL, AssetType.MUSIC, "song.mp3", "audio/mpeg", 200L * 1024 * 1024 + 1);

        assertThatThrownBy(() -> fileUploadService.issueUploadUrl(5L, request))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(FileErrorCode.FILE_TOO_LARGE);
    }
}
