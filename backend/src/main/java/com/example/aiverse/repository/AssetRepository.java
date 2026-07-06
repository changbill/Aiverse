package com.example.aiverse.repository;

import java.util.Optional;

import com.example.aiverse.entity.Asset;

public interface AssetRepository {

    Asset save(Asset asset);

    Optional<Asset> findById(Long id);
}
