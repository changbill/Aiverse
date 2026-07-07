package com.example.aiverse.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aiverse.entity.Purchase;

public interface PurchaseJpaRepository extends JpaRepository<Purchase, Long> {

    boolean existsByAssetId(Long assetId);
}
