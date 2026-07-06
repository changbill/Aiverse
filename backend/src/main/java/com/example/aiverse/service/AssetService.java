package com.example.aiverse.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.AssetErrorCode;
import com.example.aiverse.common.response.PageResponse;
import com.example.aiverse.dto.AssetDetailResponse;
import com.example.aiverse.dto.AssetListResponse;
import com.example.aiverse.entity.Asset;
import com.example.aiverse.repository.AssetRepository;
import com.example.aiverse.repository.AssetSearchCondition;
import com.example.aiverse.repository.AssetTagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetTagRepository assetTagRepository;

    public PageResponse<AssetListResponse> search(AssetSearchCondition condition, int page, int size) {
        int boundedPage = Math.max(page, 0);
        int boundedSize = Math.min(Math.max(size, 1), 100);
        return PageResponse.from(
                assetRepository.search(condition, PageRequest.of(boundedPage, boundedSize)),
                AssetListResponse::from
        );
    }

    @Transactional
    public AssetDetailResponse getDetail(Long id) {
        Asset asset = assetRepository.findPublishedDetailById(id)
                .orElseThrow(() -> new ApplicationException(AssetErrorCode.ASSET_NOT_FOUND));
        asset.increaseViewCount();
        assetRepository.save(asset);
        return AssetDetailResponse.from(asset, assetTagRepository.findByAssetId(id));
    }
}
