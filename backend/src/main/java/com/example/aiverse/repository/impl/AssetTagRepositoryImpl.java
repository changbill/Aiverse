package com.example.aiverse.repository.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.aiverse.entity.AssetTag;
import com.example.aiverse.repository.AssetTagRepository;
import com.example.aiverse.repository.jpa.AssetTagJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AssetTagRepositoryImpl implements AssetTagRepository {

    private final AssetTagJpaRepository assetTagJpaRepository;

    @Override
    public AssetTag save(AssetTag assetTag) {
        return assetTagJpaRepository.save(assetTag);
    }

    @Override
    public List<AssetTag> findByAssetId(Long assetId) {
        return assetTagJpaRepository.findByAssetId(assetId);
    }
}
