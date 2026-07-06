package com.example.aiverse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetTag;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.Category;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.entity.Tag;
import com.example.aiverse.entity.User;
import com.example.aiverse.support.RepositoryIntegrationTestSupport;
import com.example.aiverse.util.TagNameNormalizer;

class TagRepositoryTest extends RepositoryIntegrationTestSupport {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetTagRepository assetTagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void 태그를_저장하고_이름으로_조회할_수_있다() {
        tagRepository.save(Tag.of("cyberpunk"));

        assertThat(tagRepository.findByName("cyberpunk"))
                .isPresent()
                .get()
                .extracting(Tag::getName)
                .isEqualTo("cyberpunk");
    }

    @Test
    void 존재하지_않는_이름_조회시_빈값을_반환한다() {
        assertThat(tagRepository.findByName("no-such-tag")).isEmpty();
    }

    @Test
    void 태그_존재_여부를_확인할_수_있다() {
        tagRepository.save(Tag.of("city"));

        assertThat(tagRepository.existsByName("city")).isTrue();
        assertThat(tagRepository.existsByName("no-such-tag")).isFalse();
    }

    @Test
    void 사용량이_많은_순서로_태그를_조회한다() {
        User creator = userRepository.save(User.register("tag-usage@example.com", "encoded-password", "사용량태그"));
        Category category = categoryRepository.findById(1L).orElseThrow();
        Asset assetA = newAsset(creator, category, "keyA");
        Asset assetB = newAsset(creator, category, "keyB");
        Tag popular = tagRepository.save(Tag.of("usage-popular"));
        Tag rare = tagRepository.save(Tag.of("usage-rare"));
        assetTagRepository.save(AssetTag.of(assetA, popular));
        assetTagRepository.save(AssetTag.of(assetB, popular));
        assetTagRepository.save(AssetTag.of(assetA, rare));

        var result = tagRepository.searchOrderByUsage("usage-", 10);

        assertThat(result).extracting(TagUsage::name).containsExactly("usage-popular", "usage-rare");
        assertThat(result.get(0).usageCount()).isEqualTo(2);
        assertThat(result.get(1).usageCount()).isEqualTo(1);
    }

    @Test
    void 검색어로_태그_이름을_필터링한다() {
        tagRepository.save(Tag.of("filter-alpha"));
        tagRepository.save(Tag.of("filter-beta"));
        tagRepository.save(Tag.of("other-gamma"));

        var result = tagRepository.searchOrderByUsage("filter-", 10);

        assertThat(result).extracting(TagUsage::name).containsExactlyInAnyOrder("filter-alpha", "filter-beta");
    }

    @Test
    void 사용량이_0인_태그도_포함된다() {
        tagRepository.save(Tag.of("unused-tag"));

        var result = tagRepository.searchOrderByUsage("unused-", 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).usageCount()).isEqualTo(0);
    }

    @Test
    void limit_개수만큼만_반환한다() {
        for (int i = 0; i < 5; i++) {
            tagRepository.save(Tag.of("limit-tag-" + i));
        }

        var result = tagRepository.searchOrderByUsage("limit-tag-", 3);

        assertThat(result).hasSize(3);
    }

    @Test
    void 태그_이름은_공백_축소와_소문자_정규화로_저장된다() {
        String normalized = TagNameNormalizer.normalize("  Cyber   PUNK  ");
        tagRepository.save(Tag.of(normalized));

        assertThat(tagRepository.findByName("cyber punk"))
                .isPresent()
                .get()
                .extracting(Tag::getName)
                .isEqualTo("cyber punk");
        assertThat(tagRepository.findByName("cyber  punk")).isEmpty();
        assertThat(TagNameNormalizer.normalize("Cyber Punk")).isEqualTo("cyber punk");
    }

    private Asset newAsset(User creator, Category category, String objectKeySuffix) {
        return assetRepository.save(Asset.register(
                creator, "제목", null, AssetType.IMAGE, category,
                null, "original/" + objectKeySuffix + ".png", "file.png", "image/png",
                1000L, 100, null, LicenseType.PERSONAL
        ));
    }
}
