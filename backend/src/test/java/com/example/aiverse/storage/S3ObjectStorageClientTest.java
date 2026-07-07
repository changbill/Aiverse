package com.example.aiverse.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.aiverse.config.StorageProperties;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3ObjectStorageClientTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedPutObjectRequest presignedPutObjectRequest;

    private final StorageProperties storageProperties = new StorageProperties(
            "http://localhost:9000", "us-east-1", "access", "secret", "aiverse-test"
    );

    private S3ObjectStorageClient objectStorageClient() {
        return new S3ObjectStorageClient(s3Client, s3Presigner, storageProperties);
    }

    @Test
    void 업로드용_Presigned_URL을_발급한다() throws Exception {
        given(s3Presigner.presignPutObject(any(software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest.class)))
                .willReturn(presignedPutObjectRequest);
        given(presignedPutObjectRequest.url()).willReturn(URI.create("https://s3.example.com/upload").toURL());

        String url = objectStorageClient().generateUploadUrl("tmp/user-1/key.png", "image/png", Duration.ofMinutes(10));

        assertThat(url).isEqualTo("https://s3.example.com/upload");
    }

    @Test
    void 존재하는_객체를_HEAD_조회하면_메타데이터를_반환한다() {
        given(s3Client.headObject(any(HeadObjectRequest.class))).willReturn(
                HeadObjectResponse.builder().contentLength(1000L).contentType("image/png").build()
        );

        var metadata = objectStorageClient().headObject("tmp/user-1/key.png");

        assertThat(metadata).isPresent();
        assertThat(metadata.get().contentLength()).isEqualTo(1000L);
        assertThat(metadata.get().contentType()).isEqualTo("image/png");
    }

    @Test
    void 존재하지_않는_객체를_HEAD_조회하면_빈_값을_반환한다() {
        given(s3Client.headObject(any(HeadObjectRequest.class)))
                .willThrow(NoSuchKeyException.builder().message("not found").build());

        var metadata = objectStorageClient().headObject("tmp/user-1/missing.png");

        assertThat(metadata).isEmpty();
    }

    @Test
    void prefix로_객체_목록을_조회한다() {
        S3Object object = S3Object.builder().key("tmp/user-1/key.png").lastModified(Instant.now()).build();
        given(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .willReturn(ListObjectsV2Response.builder().contents(object).build());

        var summaries = objectStorageClient().listObjects("tmp/");

        assertThat(summaries).extracting(StorageObjectSummary::key).containsExactly("tmp/user-1/key.png");
    }
}
