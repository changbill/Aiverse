package com.example.aiverse.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.aiverse.dto.UploadRequest;
import com.example.aiverse.dto.UploadResponse;
import com.example.aiverse.storage.ObjectStorageClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private static final Duration UPLOAD_URL_EXPIRY = Duration.ofMinutes(10);

    private final ObjectStorageClient objectStorageClient;

    public UploadResponse issueUploadUrl(Long userId, UploadRequest request) {
        FileValidationPolicy.validate(request.purpose(), request.assetType(), request.contentType(), request.fileSize());

        String objectKey = "tmp/user-%d/%s/%s".formatted(userId, UUID.randomUUID(), request.fileName());
        String uploadUrl = objectStorageClient.generateUploadUrl(objectKey, request.contentType(), UPLOAD_URL_EXPIRY);
        LocalDateTime expiresAt = LocalDateTime.now().plus(UPLOAD_URL_EXPIRY);

        return new UploadResponse(objectKey, uploadUrl, expiresAt);
    }
}
