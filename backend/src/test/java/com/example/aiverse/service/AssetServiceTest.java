package com.example.aiverse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.example.aiverse.common.error.FileErrorCode;
import com.example.aiverse.dto.AssetCreateRequest;
import com.example.aiverse.dto.AssetDetailResponse;
import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.Category;
import com.example.aiverse.entity.CategoryName;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.entity.Tag;
import com.example.aiverse.entity.User;
import com.example.aiverse.repository.AssetRepository;
import com.example.aiverse.repository.AssetTagRepository;
import com.example.aiverse.repository.CategoryRepository;
import com.example.aiverse.repository.TagRepository;
import com.example.aiverse.repository.UserRepository;
import com.example.aiverse.storage.ObjectMetadata;
import com.example.aiverse.storage.ObjectStorageClient;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetTagRepository assetTagRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ObjectStorageClient objectStorageClient;

    private AssetService assetService;

    @BeforeEach
    void setUp() {
        assetService = new AssetService(
                assetRepository, assetTagRepository, categoryRepository, userRepository, tagRepository, objectStorageClient
        );
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

    @Test
    void HEAD_검증을_통과하면_콘텐츠를_등록하고_태그를_연결한다() {
        User creator = User.register("creator@example.com", "encoded-password", "창작자");
        setField(creator, "id", 5L);
        Category category = category(1L, CategoryName.NATURE, "nature", 1);
        given(userRepository.findById(5L)).willReturn(Optional.of(creator));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(objectStorageClient.headObject("tmp/user-5/uuid/original.png"))
                .willReturn(Optional.of(new ObjectMetadata(1000L, "image/png")));
        given(tagRepository.findByName("city")).willReturn(Optional.empty());
        given(tagRepository.save(org.mockito.ArgumentMatchers.any(Tag.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(assetTagRepository.findByAssetId(null)).willReturn(List.of());

        AssetCreateRequest request = new AssetCreateRequest(
                "제목", "설명", AssetType.IMAGE, 1L, null,
                "tmp/user-5/uuid/original.png", "original.png", "image/png", 1000L,
                100, "Midjourney", LicenseType.COMMERCIAL, List.of("city")
        );

        AssetDetailResponse result = assetService.create(5L, request);

        assertThat(result.title()).isEqualTo("제목");
        verify(assetRepository).save(org.mockito.ArgumentMatchers.any(Asset.class));
        verify(assetTagRepository).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 존재하지_않는_카테고리로_등록하면_예외를_던진다() {
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());

        AssetCreateRequest request = new AssetCreateRequest(
                "제목", "설명", AssetType.IMAGE, 999L, null,
                "tmp/user-5/uuid/original.png", "original.png", "image/png", 1000L,
                100, null, LicenseType.COMMERCIAL, null
        );

        assertThatThrownBy(() -> assetService.create(5L, request))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(AssetErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    void 다른_사용자의_임시_객체면_예외를_던진다() {
        User creator = User.register("creator@example.com", "encoded-password", "창작자");
        setField(creator, "id", 5L);
        Category category = category(1L, CategoryName.NATURE, "nature", 1);
        given(userRepository.findById(5L)).willReturn(Optional.of(creator));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

        AssetCreateRequest request = new AssetCreateRequest(
                "제목", "설명", AssetType.IMAGE, 1L, null,
                "tmp/user-999/uuid/original.png", "original.png", "image/png", 1000L,
                100, null, LicenseType.COMMERCIAL, null
        );

        assertThatThrownBy(() -> assetService.create(5L, request))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(FileErrorCode.OBJECT_VERIFICATION_FAILED);
    }

    @Test
    void 실제_객체_크기가_요청과_다르면_예외를_던진다() {
        User creator = User.register("creator@example.com", "encoded-password", "창작자");
        setField(creator, "id", 5L);
        Category category = category(1L, CategoryName.NATURE, "nature", 1);
        given(userRepository.findById(5L)).willReturn(Optional.of(creator));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(objectStorageClient.headObject(anyString()))
                .willReturn(Optional.of(new ObjectMetadata(999L, "image/png")));

        AssetCreateRequest request = new AssetCreateRequest(
                "제목", "설명", AssetType.IMAGE, 1L, null,
                "tmp/user-5/uuid/original.png", "original.png", "image/png", 1000L,
                100, null, LicenseType.COMMERCIAL, null
        );

        assertThatThrownBy(() -> assetService.create(5L, request))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(FileErrorCode.OBJECT_VERIFICATION_FAILED);
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
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
