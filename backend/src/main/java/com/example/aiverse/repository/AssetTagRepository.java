package com.example.aiverse.repository;

import java.util.List;

import com.example.aiverse.entity.AssetTag;

public interface AssetTagRepository {

    AssetTag save(AssetTag assetTag);

    List<AssetTag> findByAssetId(Long assetId);

    void deleteByAssetId(Long assetId);
}
