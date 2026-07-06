package com.example.aiverse.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.aiverse.entity.Asset;

public interface AssetRepository {

    Asset save(Asset asset);

    Optional<Asset> findById(Long id);

    Optional<Asset> findPublishedDetailById(Long id);

    Page<Asset> search(AssetSearchCondition condition, Pageable pageable);
}
