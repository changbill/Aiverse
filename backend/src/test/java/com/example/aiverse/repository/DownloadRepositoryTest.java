package com.example.aiverse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.Category;
import com.example.aiverse.entity.CreditTransaction;
import com.example.aiverse.entity.CreditTransactionType;
import com.example.aiverse.entity.Download;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.entity.Purchase;
import com.example.aiverse.entity.User;
import com.example.aiverse.support.RepositoryIntegrationTestSupport;

class DownloadRepositoryTest extends RepositoryIntegrationTestSupport {

    @Autowired
    private DownloadRepository downloadRepository;

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
    void 다운로드_이력을_저장할_수_있다() {
        User creator = userRepository.save(User.register("download-creator@example.com", "encoded-password", "다운창작자"));
        User buyer = userRepository.save(User.register("download-buyer@example.com", "encoded-password", "다운구매자"));
        Category category = categoryRepository.findById(1L).orElseThrow();
        Asset asset = assetRepository.save(Asset.register(
                creator, "다운로드 대상", null, AssetType.IMAGE, category,
                null, "original/download.png", "file.png", "image/png",
                1000L, 100, null, LicenseType.PERSONAL
        ));
        CreditTransaction transaction = creditTransactionRepository.save(
                CreditTransaction.of(buyer, null, CreditTransactionType.PURCHASE, -100, 0, "download-test")
        );
        Purchase purchase = purchaseRepository.save(
                Purchase.of(buyer, asset, transaction.getId(), "idem-download", 100, LicenseType.PERSONAL)
        );

        Download download = downloadRepository.save(Download.of(purchase, buyer, asset));

        assertThat(download.getId()).isNotNull();
        assertThat(download.getDownloadedAt()).isNotNull();
    }
}
