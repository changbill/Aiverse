package com.example.aiverse.repository.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.aiverse.entity.Purchase;
import com.example.aiverse.repository.PurchaseRepository;
import com.example.aiverse.repository.jpa.PurchaseJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PurchaseRepositoryImpl implements PurchaseRepository {

    private final PurchaseJpaRepository purchaseJpaRepository;

    @Override
    public Purchase save(Purchase purchase) {
        return purchaseJpaRepository.save(purchase);
    }

    @Override
    public boolean existsByAssetId(Long assetId) {
        return purchaseJpaRepository.existsByAssetId(assetId);
    }

    @Override
    public boolean existsByUserIdAndAssetId(Long userId, Long assetId) {
        return purchaseJpaRepository.existsByUserIdAndAssetId(userId, assetId);
    }

    @Override
    public Optional<Purchase> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey) {
        return purchaseJpaRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
    }

    @Override
    public Optional<Purchase> findByUserIdAndAssetId(Long userId, Long assetId) {
        return purchaseJpaRepository.findByUserIdAndAssetId(userId, assetId);
    }

    @Override
    public Page<Purchase> searchByUserId(Long userId, Pageable pageable) {
        return purchaseJpaRepository.searchByUserId(userId, pageable);
    }
}
