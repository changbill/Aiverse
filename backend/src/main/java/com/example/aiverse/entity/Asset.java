package com.example.aiverse.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 파일 형식/크기 검증과 HEAD 재검증(4단계 범위)은 서비스 계층 책임 — 이 Entity는 이미 검증된 값만 저장한다.
@Entity
@Table(name = "asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 20)
    private AssetType assetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "preview_object_key", length = 1024)
    private String previewObjectKey;

    @Column(name = "original_object_key", nullable = false, length = 1024)
    private String originalObjectKey;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "price_credit", nullable = false)
    private int priceCredit;

    @Column(name = "ai_tool", length = 100)
    private String aiTool;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_type", nullable = false, length = 20)
    private LicenseType licenseType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetStatus status;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private Asset(
            User creator, String title, String description, AssetType assetType, Category category,
            String previewObjectKey, String originalObjectKey, String originalFilename, String contentType,
            long fileSize, int priceCredit, String aiTool, LicenseType licenseType
    ) {
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.assetType = assetType;
        this.category = category;
        this.previewObjectKey = previewObjectKey;
        this.originalObjectKey = originalObjectKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.priceCredit = priceCredit;
        this.aiTool = aiTool;
        this.licenseType = licenseType;
        this.status = AssetStatus.PUBLISHED;
        this.viewCount = 0;
    }

    public static Asset register(
            User creator, String title, String description, AssetType assetType, Category category,
            String previewObjectKey, String originalObjectKey, String originalFilename, String contentType,
            long fileSize, int priceCredit, String aiTool, LicenseType licenseType
    ) {
        return Asset.builder()
                .creator(creator)
                .title(title)
                .description(description)
                .assetType(assetType)
                .category(category)
                .previewObjectKey(previewObjectKey)
                .originalObjectKey(originalObjectKey)
                .originalFilename(originalFilename)
                .contentType(contentType)
                .fileSize(fileSize)
                .priceCredit(priceCredit)
                .aiTool(aiTool)
                .licenseType(licenseType)
                .build();
    }

    public void increaseViewCount() {
        this.viewCount += 1;
    }

    public void updateBasicInfo(
            String title, String description, Category category,
            String previewObjectKey, Integer priceCredit, String aiTool
    ) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (category != null) this.category = category;
        if (previewObjectKey != null) this.previewObjectKey = previewObjectKey;
        if (priceCredit != null) this.priceCredit = priceCredit;
        if (aiTool != null) this.aiTool = aiTool;
    }

    public void updateOriginal(
            String originalObjectKey, String originalFilename, String contentType,
            Long fileSize, LicenseType licenseType
    ) {
        if (originalObjectKey != null) this.originalObjectKey = originalObjectKey;
        if (originalFilename != null) this.originalFilename = originalFilename;
        if (contentType != null) this.contentType = contentType;
        if (fileSize != null) this.fileSize = fileSize;
        if (licenseType != null) this.licenseType = licenseType;
    }

    public void softDelete() {
        this.status = AssetStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }
}
