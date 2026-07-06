package com.example.aiverse.repository;

import com.example.aiverse.entity.AssetType;

public record AssetSearchCondition(
        String search,
        AssetType type,
        Long categoryId,
        String tag,
        Integer minPrice,
        Integer maxPrice,
        Long creatorId,
        AssetSort sort
) {
    public AssetSort effectiveSort() {
        return sort == null ? AssetSort.LATEST : sort;
    }
}
