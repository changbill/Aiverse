package com.example.aiverse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.Category;
import com.example.aiverse.entity.CreatorSettlement;
import com.example.aiverse.entity.CreditTransaction;
import com.example.aiverse.entity.CreditTransactionType;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.entity.Purchase;
import com.example.aiverse.entity.SettlementStatus;
import com.example.aiverse.entity.User;
import com.example.aiverse.support.RepositoryIntegrationTestSupport;

import jakarta.persistence.EntityManager;

class CreatorSettlementRepositoryTest extends RepositoryIntegrationTestSupport {

    @Autowired
    private CreatorSettlementRepository creatorSettlementRepository;

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

    @Autowired
    private EntityManager entityManager;

    @Test
    void 창작자_정산을_저장하면_판매가와_수수료_합이_유지된다() {
        User creator = userRepository.save(User.register("settlement-creator@example.com", "encoded-password", "정산창작자"));
        User buyer = userRepository.save(User.register("settlement-buyer@example.com", "encoded-password", "정산구매자"));
        Category category = categoryRepository.findById(1L).orElseThrow();
        Asset asset = assetRepository.save(Asset.register(
                creator, "정산 대상", null, AssetType.IMAGE, category,
                null, "original/settlement.png", "file.png", "image/png",
                1000L, 100, null, LicenseType.PERSONAL
        ));
        CreditTransaction transaction = creditTransactionRepository.save(
                CreditTransaction.of(buyer, null, CreditTransactionType.PURCHASE, -100, 0, "settlement-test")
        );
        Purchase purchase = purchaseRepository.save(
                Purchase.of(buyer, asset, transaction.getId(), "idem-settlement", 100, LicenseType.PERSONAL)
        );

        CreatorSettlement settlement = creatorSettlementRepository.save(
                CreatorSettlement.settle(creator, purchase, asset, 100, 20, 80)
        );

        assertThat(settlement.getId()).isNotNull();
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.SETTLED);
        assertThat(settlement.getSettledAt()).isNotNull();
        assertThat(settlement.getPlatformFeeCredit() + settlement.getSettlementCredit()).isEqualTo(settlement.getGrossCredit());
    }

    @Test
    void 기간_시작일_이후_판매만_합산한다() {
        User creator = userRepository.save(User.register("totals-creator@example.com", "encoded-password", "합계창작자"));
        User buyer = userRepository.save(User.register("totals-buyer@example.com", "encoded-password", "합계구매자"));
        Category category = categoryRepository.findById(1L).orElseThrow();
        Asset assetA = assetRepository.save(Asset.register(
                creator, "합계 대상A", null, AssetType.IMAGE, category,
                null, "original/totals-a.png", "file.png", "image/png",
                1000L, 100, null, LicenseType.PERSONAL
        ));
        Asset assetB = assetRepository.save(Asset.register(
                creator, "합계 대상B", null, AssetType.IMAGE, category,
                null, "original/totals-b.png", "file.png", "image/png",
                1000L, 100, null, LicenseType.PERSONAL
        ));
        Asset assetC = assetRepository.save(Asset.register(
                creator, "합계 대상C", null, AssetType.IMAGE, category,
                null, "original/totals-c.png", "file.png", "image/png",
                1000L, 100, null, LicenseType.PERSONAL
        ));
        LocalDateTime now = LocalDateTime.now();

        sale(creator, buyer, assetA, 100, 20, 80, now.minusDays(10));
        sale(creator, buyer, assetB, 100, 20, 80, now.minusDays(3));
        sale(creator, buyer, assetC, 100, 20, 80, now.minusDays(40));

        CreatorSalesTotals last7Days = creatorSettlementRepository.sumSales(creator.getId(), now.minusDays(6));
        assertThat(last7Days.salesCount()).isEqualTo(1);
        assertThat(last7Days.revenueCredit()).isEqualTo(80);

        CreatorSalesTotals allTime = creatorSettlementRepository.sumSales(creator.getId(), null);
        assertThat(allTime.salesCount()).isEqualTo(3);
        assertThat(allTime.revenueCredit()).isEqualTo(240);
    }

    @Test
    void 다른_창작자의_판매는_집계에_섞이지_않는다() {
        User creatorA = userRepository.save(User.register("owner-a@example.com", "encoded-password", "창작자A"));
        User creatorB = userRepository.save(User.register("owner-b@example.com", "encoded-password", "창작자B"));
        User buyer = userRepository.save(User.register("owner-buyer@example.com", "encoded-password", "소유권구매자"));
        Category category = categoryRepository.findById(1L).orElseThrow();
        Asset assetA = assetRepository.save(Asset.register(
                creatorA, "A 콘텐츠", null, AssetType.IMAGE, category,
                null, "original/owner-a.png", "file.png", "image/png",
                1000L, 100, null, LicenseType.PERSONAL
        ));
        Asset assetB = assetRepository.save(Asset.register(
                creatorB, "B 콘텐츠", null, AssetType.IMAGE, category,
                null, "original/owner-b.png", "file.png", "image/png",
                1000L, 100, null, LicenseType.PERSONAL
        ));

        sale(creatorA, buyer, assetA, 100, 20, 80, LocalDateTime.now());
        sale(creatorB, buyer, assetB, 100, 20, 80, LocalDateTime.now());

        CreatorSalesTotals totalsA = creatorSettlementRepository.sumSales(creatorA.getId(), null);
        assertThat(totalsA.salesCount()).isEqualTo(1);
        assertThat(totalsA.revenueCredit()).isEqualTo(80);
    }

    private void sale(
            User creator, User buyer, Asset asset,
            int price, int platformFee, int revenue, LocalDateTime settledAt
    ) {
        CreditTransaction transaction = creditTransactionRepository.save(
                CreditTransaction.of(buyer, null, CreditTransactionType.PURCHASE, -price, 0, "totals-test")
        );
        Purchase purchase = purchaseRepository.save(Purchase.of(
                buyer, asset, transaction.getId(), "idem-" + System.nanoTime(), price, LicenseType.PERSONAL
        ));
        CreatorSettlement settlement = creatorSettlementRepository.save(
                CreatorSettlement.settle(creator, purchase, asset, price, platformFee, revenue)
        );
        entityManager.createNativeQuery("UPDATE creator_settlement SET settled_at = :settledAt WHERE id = :id")
                .setParameter("settledAt", settledAt)
                .setParameter("id", settlement.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }
}
