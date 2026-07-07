package com.example.aiverse.repository.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetStatus;
import com.example.aiverse.repository.AssetRepository;
import com.example.aiverse.repository.AssetSearchCondition;
import com.example.aiverse.repository.jpa.AssetJpaRepository;
import com.example.aiverse.repository.querydsl.AssetQuerydslRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AssetRepositoryImpl implements AssetRepository {

    private final AssetJpaRepository assetJpaRepository;
    private final AssetQuerydslRepository assetQuerydslRepository;

    @Override
    public Asset save(Asset asset) {
        return assetJpaRepository.save(asset);
    }

    @Override
    public Optional<Asset> findById(Long id) {
        return assetJpaRepository.findById(id);
    }

    @Override
    public Optional<Asset> findPublishedDetailById(Long id) {
        return assetJpaRepository.findByIdAndStatus(id, AssetStatus.PUBLISHED);
    }

    @Override
    public Optional<Asset> findPurchasableById(Long id) {
        return assetJpaRepository.findPurchasableById(id, AssetStatus.PUBLISHED);
    }

    @Override
    public boolean existsByObjectKey(String objectKey) {
        return assetJpaRepository.existsByObjectKey(objectKey);
    }

    @Override
    public long countByCreatorId(Long creatorId) {
        return assetJpaRepository.countByCreatorId(creatorId);
    }

    @Override
    public Page<Asset> search(AssetSearchCondition condition, Pageable pageable) {
        return assetQuerydslRepository.search(condition, pageable);
    }
}
