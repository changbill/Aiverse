package com.example.aiverse.repository;

import static org.assertj.core.api.Assertions.assertThat;

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
}
