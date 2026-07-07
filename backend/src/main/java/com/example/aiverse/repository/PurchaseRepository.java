package com.example.aiverse.repository;

public interface PurchaseRepository {

    boolean existsByAssetId(Long assetId);
}
