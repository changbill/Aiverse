package com.example.aiverse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetStatus;
import com.example.aiverse.entity.AssetTag;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.Category;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.entity.Tag;
import com.example.aiverse.entity.User;
import com.example.aiverse.support.IntegrationTestSupport;
import com.example.aiverse.support.TestPurchaseSupport;

import jakarta.persistence.EntityManager;

class AssetRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AssetTagRepository assetTagRepository;

    @Autowired
    private EntityManager entityManager;

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

    @Test
    void 태그_필터는_공백과_대소문자를_정규화해_조회한다() {
        User creator = userRepository.save(User.register("tag-filter@example.com", "encoded-password", "태그필터"));
        Category category = categoryRepository.findById(1L).orElseThrow();
        Asset asset = assetRepository.save(asset(creator, category, "태그 대상", AssetType.IMAGE, 100));
        Tag tag = tagRepository.save(Tag.of("cyber punk"));
        assetTagRepository.save(AssetTag.of(asset, tag));
        assetRepository.save(asset(creator, category, "다른 콘텐츠", AssetType.IMAGE, 200));

        var condition = new AssetSearchCondition(
                null, null, null, "  CYBER   PUNK  ",
                null, null, null, AssetSort.LATEST
        );
        var result = assetRepository.search(condition, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(Asset::getTitle).containsExactly("태그 대상");
    }

    @Test
    void 최신순으로_콘텐츠를_정렬한다() {
        User creator = userRepository.save(User.register("latest@example.com", "encoded-password", "최신순"));
        Category category = categoryRepository.findById(1L).orElseThrow();
        Asset older = assetRepository.save(asset(creator, category, "이전 콘텐츠", AssetType.IMAGE, 100));
        Asset newer = assetRepository.save(asset(creator, category, "최신 콘텐츠", AssetType.IMAGE, 100));

        var condition = new AssetSearchCondition(
                null, null, null, null,
                null, null, creator.getId(), AssetSort.LATEST
        );
        var result = assetRepository.search(condition, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Asset::getTitle)
                .containsExactly("최신 콘텐츠", "이전 콘텐츠");
        assertThat(result.getContent().getFirst().getId()).isEqualTo(newer.getId());
        assertThat(result.getContent().get(1).getId()).isEqualTo(older.getId());
    }

    @Test
    void 가격_내림차순으로_콘텐츠를_정렬한다() {
        User creator = userRepository.save(User.register("price-desc@example.com", "encoded-password", "가격내림"));
        Category category = categoryRepository.findById(1L).orElseThrow();
        assetRepository.save(asset(creator, category, "저가", AssetType.IMAGE, 50));
        assetRepository.save(asset(creator, category, "고가", AssetType.IMAGE, 300));
        assetRepository.save(asset(creator, category, "중가", AssetType.IMAGE, 150));

        var condition = new AssetSearchCondition(
                null, null, null, null,
                null, null, creator.getId(), AssetSort.PRICE_DESC
        );
        var result = assetRepository.search(condition, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Asset::getTitle)
                .containsExactly("고가", "중가", "저가");
    }

    @Test
    void 인기순으로_구매_횟수가_많은_콘텐츠를_우선_정렬한다() {
        User creator = userRepository.save(User.register("popular-creator@example.com", "encoded-password", "인기창작자"));
        User buyerA = userRepository.save(User.register("buyer-a@example.com", "encoded-password", "구매자A"));
        User buyerB = userRepository.save(User.register("buyer-b@example.com", "encoded-password", "구매자B"));
        User buyerC = userRepository.save(User.register("buyer-c@example.com", "encoded-password", "구매자C"));
        Category category = categoryRepository.findById(1L).orElseThrow();
        Asset popular = assetRepository.save(asset(creator, category, "인기 콘텐츠", AssetType.IMAGE, 100));
        Asset moderate = assetRepository.save(asset(creator, category, "보통 콘텐츠", AssetType.IMAGE, 100));
        assetRepository.save(asset(creator, category, "비인기 콘텐츠", AssetType.IMAGE, 100));

        TestPurchaseSupport.insertPurchase(entityManager, buyerA, popular, "popular-a");
        TestPurchaseSupport.insertPurchase(entityManager, buyerB, popular, "popular-b");
        TestPurchaseSupport.insertPurchase(entityManager, buyerC, moderate, "moderate-c");

        var condition = new AssetSearchCondition(
                null, null, null, null,
                null, null, creator.getId(), AssetSort.POPULAR
        );
        var result = assetRepository.search(condition, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Asset::getTitle)
                .containsExactly("인기 콘텐츠", "보통 콘텐츠", "비인기 콘텐츠");
    }

    private Asset asset(User creator, Category category, String title, AssetType type, int price) {
        return Asset.register(
                creator, title, "검색 설명", type, category,
                null, "original/" + title, title, "application/octet-stream",
                1000L, price, null, LicenseType.PERSONAL
        );
    }
}
