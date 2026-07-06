package com.example.aiverse.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aiverse.common.response.PageResponse;
import com.example.aiverse.dto.AssetListResponse;
import com.example.aiverse.repository.AssetRepository;
import com.example.aiverse.repository.AssetSearchCondition;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetService {

    private final AssetRepository assetRepository;

    public PageResponse<AssetListResponse> search(AssetSearchCondition condition, int page, int size) {
        int boundedPage = Math.max(page, 0);
        int boundedSize = Math.min(Math.max(size, 1), 100);
        return PageResponse.from(
                assetRepository.search(condition, PageRequest.of(boundedPage, boundedSize)),
                AssetListResponse::from
        );
    }
}
