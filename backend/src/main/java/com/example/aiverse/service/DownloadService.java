package com.example.aiverse.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.PurchaseErrorCode;
import com.example.aiverse.dto.DownloadRequest;
import com.example.aiverse.dto.DownloadResponse;
import com.example.aiverse.entity.Download;
import com.example.aiverse.entity.Purchase;
import com.example.aiverse.repository.DownloadRepository;
import com.example.aiverse.repository.PurchaseRepository;
import com.example.aiverse.storage.ObjectStorageClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DownloadService {

    private static final Duration DOWNLOAD_URL_EXPIRY = Duration.ofMinutes(5);

    private final PurchaseRepository purchaseRepository;
    private final DownloadRepository downloadRepository;
    private final ObjectStorageClient objectStorageClient;

    @Transactional
    public DownloadResponse download(Long userId, DownloadRequest request) {
        Purchase purchase = purchaseRepository.findByUserIdAndAssetId(userId, request.assetId())
                .orElseThrow(() -> new ApplicationException(PurchaseErrorCode.PURCHASE_NOT_FOUND));

        downloadRepository.save(Download.of(purchase, purchase.getUser(), purchase.getAsset()));

        String downloadUrl = objectStorageClient.generateDownloadUrl(
                purchase.getAsset().getOriginalObjectKey(), DOWNLOAD_URL_EXPIRY
        );
        LocalDateTime expiresAt = LocalDateTime.now().plus(DOWNLOAD_URL_EXPIRY);

        return new DownloadResponse(downloadUrl, expiresAt);
    }
}
