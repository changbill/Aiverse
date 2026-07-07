package com.example.aiverse.repository.impl;

import org.springframework.stereotype.Repository;

import com.example.aiverse.repository.PurchaseRepository;
import com.example.aiverse.repository.jpa.PurchaseJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PurchaseRepositoryImpl implements PurchaseRepository {

    private final PurchaseJpaRepository purchaseJpaRepository;

    @Override
    public boolean existsByAssetId(Long assetId) {
        return purchaseJpaRepository.existsByAssetId(assetId);
    }
}
