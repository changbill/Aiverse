package com.example.aiverse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
}
