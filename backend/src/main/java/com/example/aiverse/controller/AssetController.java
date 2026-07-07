package com.example.aiverse.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.aiverse.common.response.ApiResponse;
import com.example.aiverse.common.response.PageResponse;
import com.example.aiverse.dto.AssetCreateRequest;
import com.example.aiverse.dto.AssetDetailResponse;
import com.example.aiverse.dto.AssetListResponse;
import com.example.aiverse.dto.AssetUpdateRequest;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.repository.AssetSearchCondition;
import com.example.aiverse.repository.AssetSort;
import com.example.aiverse.service.AssetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Content")
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @Operation(summary = "콘텐츠 목록 검색")
    @GetMapping
    public PageResponse<AssetListResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AssetType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(defaultValue = "LATEST") AssetSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return assetService.search(
                new AssetSearchCondition(search, type, categoryId, tag, minPrice, maxPrice, creatorId, sort),
                page,
                size
        );
    }

    @Operation(summary = "콘텐츠 상세 조회")
    @GetMapping("/{id}")
    public ApiResponse<AssetDetailResponse> getDetail(@PathVariable Long id) {
        return ApiResponse.of(assetService.getDetail(id));
    }

    @Operation(summary = "콘텐츠 등록")
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AssetDetailResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AssetCreateRequest request
    ) {
        return ApiResponse.of(assetService.create(userId, request));
    }

    @Operation(summary = "콘텐츠 수정")
    @SecurityRequirement(name = "bearer-jwt")
    @PutMapping("/{id}")
    public ApiResponse<AssetDetailResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody AssetUpdateRequest request
    ) {
        return ApiResponse.of(assetService.update(userId, id, request));
    }

    @Operation(summary = "콘텐츠 삭제")
    @SecurityRequirement(name = "bearer-jwt")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        assetService.delete(userId, id);
    }
}
