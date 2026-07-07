package com.example.aiverse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.example.aiverse.common.response.PageResponse;
import com.example.aiverse.dto.LibraryItemResponse;
import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.Category;
import com.example.aiverse.entity.CategoryName;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.entity.Purchase;
import com.example.aiverse.entity.User;
import com.example.aiverse.repository.PurchaseRepository;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    private LibraryService libraryService;

    @BeforeEach
    void setUp() {
        libraryService = new LibraryService(purchaseRepository);
    }

    @Test
    void 구매_보관함을_페이지로_반환한다() {
        User creator = User.register("library-creator@example.com", "encoded-password", "보관함창작자");
        Category category = category(1L, CategoryName.NATURE, "nature", 1);
        Asset asset = Asset.register(
                creator, "보관함 콘텐츠", null, AssetType.IMAGE, category,
                "preview/key.jpg", "original/key.png", "file.png", "image/png",
                1000L, 100, null, LicenseType.PERSONAL
        );
        User buyer = User.register("library-buyer@example.com", "encoded-password", "보관함구매자");
        Purchase purchase = Purchase.of(buyer, asset, 1L, "idem-lib", 100, LicenseType.PERSONAL);
        given(purchaseRepository.searchByUserId(eq(5L), eq(PageRequest.of(0, 20))))
                .willReturn(new PageImpl<>(List.of(purchase), PageRequest.of(0, 20), 1));

        PageResponse<LibraryItemResponse> result = libraryService.getLibrary(5L, 0, 20);

        assertThat(result.data()).extracting(item -> item.asset().title()).containsExactly("보관함 콘텐츠");
        assertThat(result.page().totalElements()).isEqualTo(1);
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
