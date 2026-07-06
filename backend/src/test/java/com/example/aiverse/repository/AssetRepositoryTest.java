package com.example.aiverse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetStatus;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.Category;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.entity.User;
import com.example.aiverse.support.IntegrationTestSupport;

class AssetRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void 콘텐츠를_저장하고_ID로_조회할_수_있다() {
        User creator = userRepository.save(User.register("creator@example.com", "encoded-password", "창작자"));
        Category category = categoryRepository.findById(1L).orElseThrow();

        Asset asset = Asset.register(
                creator, "노을 지는 도시", "사이버펑크 스타일", AssetType.IMAGE, category,
                "preview/key.jpg", "original/key.png", "sunset.png", "image/png",
                4_200_000L, 120, "Midjourney", LicenseType.COMMERCIAL
        );
        Asset saved = assetRepository.save(asset);

        assertThat(saved.getId()).isNotNull();
        assertThat(assetRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getTitle()).isEqualTo("노을 지는 도시");
                    assertThat(found.getStatus()).isEqualTo(AssetStatus.PUBLISHED);
                    assertThat(found.getViewCount()).isEqualTo(0);
                    assertThat(found.getCreator().getId()).isEqualTo(creator.getId());
                    assertThat(found.getCategory().getId()).isEqualTo(category.getId());
                });
    }

    @Test
    void 조회수를_증가시킬_수_있다() {
        User creator = userRepository.save(User.register("creator2@example.com", "encoded-password", "창작자2"));
        Category category = categoryRepository.findById(1L).orElseThrow();
        Asset asset = assetRepository.save(Asset.register(
                creator, "제목", null, AssetType.IMAGE, category,
                null, "original/key2.png", "file.png", "image/png",
                1000L, 100, null, LicenseType.PERSONAL
        ));

        asset.increaseViewCount();
        assetRepository.save(asset);

        assertThat(assetRepository.findById(asset.getId()))
                .isPresent()
                .get()
                .extracting(Asset::getViewCount)
                .isEqualTo(1L);
    }

    @Test
    void 조건에_맞는_콘텐츠를_가격순으로_페이징한다() {
        User creator = userRepository.save(User.register("search@example.com", "encoded-password", "검색창작자"));
        Category category = categoryRepository.findById(1L).orElseThrow();
        assetRepository.save(asset(creator, category, "저가 이미지", AssetType.IMAGE, 50));
        assetRepository.save(asset(creator, category, "중가 이미지", AssetType.IMAGE, 100));
        assetRepository.save(asset(creator, category, "고가 음악", AssetType.MUSIC, 200));

        var condition = new AssetSearchCondition(
                "이미지", AssetType.IMAGE, category.getId(), null,
                50, 150, creator.getId(), AssetSort.PRICE_ASC
        );
        var result = assetRepository.search(condition, PageRequest.of(0, 1));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.getContent())
                .extracting(Asset::getTitle)
                .containsExactly("저가 이미지");
        assertThat(result.getContent().getFirst().getCreator().getNickname()).isEqualTo("검색창작자");
    }

    private Asset asset(User creator, Category category, String title, AssetType type, int price) {
        return Asset.register(
                creator, title, "검색 설명", type, category,
                null, "original/" + title, title, "application/octet-stream",
                1000L, price, null, LicenseType.PERSONAL
        );
    }
}
