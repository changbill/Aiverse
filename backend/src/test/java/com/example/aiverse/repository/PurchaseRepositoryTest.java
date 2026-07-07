package com.example.aiverse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetStatus;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.Category;
import com.example.aiverse.entity.CreditTransaction;
import com.example.aiverse.entity.CreditTransactionType;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.entity.Purchase;
import com.example.aiverse.entity.User;
import com.example.aiverse.support.RepositoryIntegrationTestSupport;

class PurchaseRepositoryTest extends RepositoryIntegrationTestSupport {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private CreditTransactionRepository creditTransactionRepository;

    @Test
    void 구매를_저장하고_사용자와_Idempotency_Key로_조회할_수_있다() {
        User buyer = userRepository.save(User.register("buyer1@example.com", "encoded-password", "구매자1"));
        Asset asset = asset(1L);
        Purchase purchase = purchaseRepository.save(
                Purchase.of(buyer, asset, creditTransactionId(buyer), "idem-1", 120, LicenseType.COMMERCIAL)
        );

        assertThat(purchase.getId()).isNotNull();
        assertThat(purchaseRepository.findByUserIdAndIdempotencyKey(buyer.getId(), "idem-1"))
                .isPresent()
                .get()
                .extracting(Purchase::getId)
                .isEqualTo(purchase.getId());
        assertThat(purchaseRepository.findByUserIdAndIdempotencyKey(buyer.getId(), "no-such-key")).isEmpty();
    }

    @Test
    void 사용자와_콘텐츠로_구매_존재_여부를_확인할_수_있다() {
        User buyer = userRepository.save(User.register("buyer2@example.com", "encoded-password", "구매자2"));
        Asset asset = asset(1L);
        purchaseRepository.save(Purchase.of(buyer, asset, creditTransactionId(buyer), "idem-2", 120, LicenseType.PERSONAL));

        assertThat(purchaseRepository.existsByUserIdAndAssetId(buyer.getId(), asset.getId())).isTrue();
        assertThat(purchaseRepository.existsByAssetId(asset.getId())).isTrue();
        assertThat(purchaseRepository.findByUserIdAndAssetId(buyer.getId(), asset.getId()))
                .isPresent()
                .get()
                .extracting(p -> p.getAsset().getId())
                .isEqualTo(asset.getId());
    }

    @Test
    void 사용자의_구매_이력을_최신순으로_페이지네이션한다() {
        User buyer = userRepository.save(User.register("buyer3@example.com", "encoded-password", "구매자3"));
        Asset assetA = asset(1L);
        Asset assetB = asset(1L);
        purchaseRepository.save(Purchase.of(buyer, assetA, creditTransactionId(buyer), "idem-a", 100, LicenseType.PERSONAL));
        purchaseRepository.save(Purchase.of(buyer, assetB, creditTransactionId(buyer), "idem-b", 200, LicenseType.PERSONAL));

        var result = purchaseRepository.searchByUserId(buyer.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().getFirst().getIdempotencyKey()).isEqualTo("idem-b");
    }

    @Test
    void 구매_이후_콘텐츠_가격이_바뀌어도_구매_당시_가격과_라이선스는_그대로_유지된다() {
        User buyer = userRepository.save(User.register("buyer4@example.com", "encoded-password", "구매자4"));
        Asset asset = asset(1L);
        Purchase purchase = purchaseRepository.save(
                Purchase.of(buyer, asset, creditTransactionId(buyer), "idem-snapshot", asset.getPriceCredit(), asset.getLicenseType())
        );

        asset.updateBasicInfo(null, null, null, null, 999, null);
        assetRepository.save(asset);

        Purchase reloaded = purchaseRepository.findByUserIdAndIdempotencyKey(buyer.getId(), "idem-snapshot").orElseThrow();
        assertThat(reloaded.getPurchasePriceCredit()).isEqualTo(120);
        assertThat(reloaded.getLicenseType()).isEqualTo(LicenseType.COMMERCIAL);
        assertThat(assetRepository.findById(asset.getId()).orElseThrow().getPriceCredit()).isEqualTo(999);
        assertThat(purchase.getPurchasePriceCredit()).isEqualTo(120);
    }

    @Test
    void 소프트_삭제된_콘텐츠도_구매_보관함에서는_계속_조회된다() {
        User buyer = userRepository.save(User.register("buyer5@example.com", "encoded-password", "구매자5"));
        Asset asset = asset(1L);
        purchaseRepository.save(Purchase.of(buyer, asset, creditTransactionId(buyer), "idem-deleted", 120, LicenseType.COMMERCIAL));

        asset.softDelete();
        assetRepository.save(asset);

        var library = purchaseRepository.searchByUserId(buyer.getId(), PageRequest.of(0, 10));

        assertThat(library.getTotalElements()).isEqualTo(1);
        assertThat(library.getContent().getFirst().getAsset().getStatus()).isEqualTo(AssetStatus.DELETED);
    }

    private Asset asset(Long categoryId) {
        User creator = userRepository.save(User.register(
                "creator-" + System.nanoTime() + "@example.com", "encoded-password", "창작자" + System.nanoTime() % 100000
        ));
        Category category = categoryRepository.findById(categoryId).orElseThrow();
        return assetRepository.save(Asset.register(
                creator, "구매 대상", "설명", AssetType.IMAGE, category,
                null, "original/purchase-test.png", "file.png", "image/png",
                1000L, 120, null, LicenseType.COMMERCIAL
        ));
    }

    private Long creditTransactionId(User buyer) {
        CreditTransaction transaction = creditTransactionRepository.save(
                CreditTransaction.of(buyer, null, CreditTransactionType.PURCHASE, -120, 0, "purchase-test")
        );
        return transaction.getId();
    }
}
