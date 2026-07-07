package com.example.aiverse.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.AssetErrorCode;
import com.example.aiverse.common.error.AuthErrorCode;
import com.example.aiverse.common.error.FileErrorCode;
import com.example.aiverse.common.response.PageResponse;
import com.example.aiverse.dto.AssetCreateRequest;
import com.example.aiverse.dto.AssetDetailResponse;
import com.example.aiverse.dto.AssetListResponse;
import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetTag;
import com.example.aiverse.entity.Category;
import com.example.aiverse.entity.Tag;
import com.example.aiverse.entity.User;
import com.example.aiverse.repository.AssetRepository;
import com.example.aiverse.repository.AssetSearchCondition;
import com.example.aiverse.repository.AssetTagRepository;
import com.example.aiverse.repository.CategoryRepository;
import com.example.aiverse.repository.TagRepository;
import com.example.aiverse.repository.UserRepository;
import com.example.aiverse.storage.ObjectMetadata;
import com.example.aiverse.storage.ObjectStorageClient;
import com.example.aiverse.util.TagNameNormalizer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetTagRepository assetTagRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ObjectStorageClient objectStorageClient;

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

    @Transactional
    public AssetDetailResponse create(Long userId, AssetCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ApplicationException(AssetErrorCode.CATEGORY_NOT_FOUND));
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(AuthErrorCode.AUTHENTICATION_REQUIRED));

        verifyOriginalObject(userId, request.originalObjectKey(), request.contentType(), request.fileSize());
        if (request.previewObjectKey() != null) {
            verifyObjectExists(userId, request.previewObjectKey());
        }

        Asset asset = Asset.register(
                creator, request.title(), request.description(), request.assetType(), category,
                request.previewObjectKey(), request.originalObjectKey(), request.originalFilename(),
                request.contentType(), request.fileSize(), request.priceCredit(), request.aiTool(), request.licenseType()
        );
        assetRepository.save(asset);
        attachTags(asset, request.tags());

        return AssetDetailResponse.from(asset, assetTagRepository.findByAssetId(asset.getId()));
    }

    private void verifyOriginalObject(Long userId, String objectKey, String expectedContentType, long expectedSize) {
        ObjectMetadata metadata = verifyObjectExists(userId, objectKey);
        if (metadata.contentLength() != expectedSize || !metadata.contentType().equals(expectedContentType)) {
            throw new ApplicationException(FileErrorCode.OBJECT_VERIFICATION_FAILED);
        }
    }

    private ObjectMetadata verifyObjectExists(Long userId, String objectKey) {
        if (!objectKey.startsWith("tmp/user-" + userId + "/")) {
            throw new ApplicationException(FileErrorCode.OBJECT_VERIFICATION_FAILED);
        }
        return objectStorageClient.headObject(objectKey)
                .orElseThrow(() -> new ApplicationException(FileErrorCode.OBJECT_VERIFICATION_FAILED));
    }

    private void attachTags(Asset asset, List<String> tagNames) {
        if (tagNames == null) {
            return;
        }
        for (String rawName : tagNames) {
            String normalized = TagNameNormalizer.normalize(rawName);
            if (normalized == null || normalized.isEmpty()) {
                continue;
            }
            Tag tag = tagRepository.findByName(normalized).orElseGet(() -> tagRepository.save(Tag.of(normalized)));
            assetTagRepository.save(AssetTag.of(asset, tag));
        }
    }
}
