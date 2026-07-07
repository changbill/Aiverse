package com.example.aiverse.storage;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface ObjectStorageClient {

    String generateUploadUrl(String objectKey, String contentType, Duration expiry);

    Optional<ObjectMetadata> headObject(String objectKey);

    void deleteObject(String objectKey);

    List<StorageObjectSummary> listObjects(String prefix);
}
