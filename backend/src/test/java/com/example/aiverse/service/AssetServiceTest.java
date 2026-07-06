package com.example.aiverse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.AssetErrorCode;
import com.example.aiverse.dto.AssetDetailResponse;
import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.Category;
import com.example.aiverse.entity.CategoryName;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.entity.User;
import com.example.aiverse.repository.AssetRepository;
import com.example.aiverse.repository.AssetTagRepository;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetTagRepository assetTagRepository;

    private AssetService assetService;

    @BeforeEach
    void setUp() {
        assetService = new AssetService(assetRepository, assetTagRepository);
    }

    @Test
    void 상세_조회_시_조회수를_증가시키고_태그를_포함해_반환한다() {
        User creator = User.register("creator@example.com", "encoded-password", "창작자");
        Category category = category(1L, CategoryName.NATURE, "nature", 1);
        Asset asset = Asset.register(
                creator, "제목", "설명", AssetType.IMAGE, category,
                "preview/key.jpg", "original/key.png", "file.png", "image/png",
                1000L, 100, "Midjourney", LicenseType.COMMERCIAL
        );
        given(assetRepository.findPublishedDetailById(1L)).willReturn(Optional.of(asset));
        given(assetTagRepository.findByAssetId(1L)).willReturn(List.of());

        AssetDetailResponse result = assetService.getDetail(1L);

        assertThat(result.viewCount()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("제목");
        verify(assetRepository).save(asset);
    }

    @Test
    void 존재하지_않는_콘텐츠_조회_시_예외를_던진다() {
        given(assetRepository.findPublishedDetailById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.getDetail(999L))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(AssetErrorCode.ASSET_NOT_FOUND);
    }

    private Category category(Long id, CategoryName name, String slug, int displayOrder) {
        try {
            var constructor = Category.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Category category = constructor.newInstance();
            setField(category, "id", id);
            setField(category, "name", name);
            setField(category, "slug", slug);
            setField(category, "displayOrder", displayOrder);
            setField(category, "active", true);
            return category;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = Category.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
