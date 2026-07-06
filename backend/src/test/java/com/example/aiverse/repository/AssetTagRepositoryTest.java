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

class AssetTagRepositoryTest extends RepositoryIntegrationTestSupport {

    @Autowired
    private AssetTagRepository assetTagRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void 콘텐츠에_연결된_태그_목록을_조회한다() {
        User creator = userRepository.save(User.register("tag-owner@example.com", "encoded-password", "태그주인"));
        Category category = categoryRepository.findById(1L).orElseThrow();
        Asset asset = assetRepository.save(Asset.register(
                creator, "제목", null, AssetType.IMAGE, category,
                null, "original/key3.png", "file.png", "image/png",
                1000L, 100, null, LicenseType.PERSONAL
        ));
        Tag cyberpunk = tagRepository.save(Tag.of("cyberpunk"));
        Tag city = tagRepository.save(Tag.of("city"));

        assetTagRepository.save(AssetTag.of(asset, cyberpunk));
        assetTagRepository.save(AssetTag.of(asset, city));

        var result = assetTagRepository.findByAssetId(asset.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(assetTag -> assetTag.getTag().getName())
                .containsExactlyInAnyOrder("cyberpunk", "city");
    }

    @Test
    void 연결된_태그가_없으면_빈_목록을_반환한다() {
        assertThat(assetTagRepository.findByAssetId(999L)).isEmpty();
    }
}
