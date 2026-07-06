package com.example.aiverse.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.aiverse.entity.Asset;
import com.example.aiverse.repository.AssetRepository;
import com.example.aiverse.repository.jpa.AssetJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AssetRepositoryImpl implements AssetRepository {

    private final AssetJpaRepository assetJpaRepository;

    @Override
    public Asset save(Asset asset) {
        return assetJpaRepository.save(asset);
    }

    @Override
    public Optional<Asset> findById(Long id) {
        return assetJpaRepository.findById(id);
    }
}
