package com.example.aiverse.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.aiverse.entity.Purchase;

public interface PurchaseRepository {

    Purchase save(Purchase purchase);

    boolean existsByAssetId(Long assetId);

    boolean existsByUserIdAndAssetId(Long userId, Long assetId);

    Optional<Purchase> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    Optional<Purchase> findByUserIdAndAssetId(Long userId, Long assetId);

    Page<Purchase> searchByUserId(Long userId, Pageable pageable);
}
