package com.example.aiverse.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.aiverse.dto.PurchaseRequest;
import com.example.aiverse.dto.PurchaseResponse;
import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.Category;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.entity.User;
import com.example.aiverse.repository.AssetRepository;
import com.example.aiverse.repository.CategoryRepository;
import com.example.aiverse.repository.UserRepository;
import com.example.aiverse.support.IntegrationTestSupport;

import jakarta.persistence.EntityManager;

/**
 * 서로가 서로의 창작자·구매자가 되는 상황에서 두 구매를 동시에 요청해도
 * 사용자 ID 오름차순 잠금 덕분에 교착 상태 없이 둘 다 처리되는지 검증한다.
 * 잠금 정렬이 없다면 스레드1(창작자X→구매자Y)은 Y→X 순으로, 스레드2
 * (구매자X→창작자Y)는 X→Y 순으로 잠가 서로 반대 순서로 대기하며 교착된다.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PurchaseConcurrencyTest extends IntegrationTestSupport {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long userXId;
    private Long userYId;
    private Long assetOwnedByXId;
    private Long assetOwnedByYId;

    @BeforeEach
    void setUp() {
        User userX = userRepository.save(
                User.register("cross-x-" + UUID.randomUUID() + "@example.com", "encoded-password", "X" + System.nanoTime() % 100000)
        );
        User userY = userRepository.save(
                User.register("cross-y-" + UUID.randomUUID() + "@example.com", "encoded-password", "Y" + System.nanoTime() % 100000)
        );
        userX.increaseCredit(500);
        userY.increaseCredit(500);
        userRepository.save(userX);
        userRepository.save(userY);
        userXId = userX.getId();
        userYId = userY.getId();

        Category category = categoryRepository.findById(1L).orElseThrow();
        Asset assetOwnedByX = assetRepository.save(Asset.register(
                userX, "X 소유 콘텐츠", null, AssetType.IMAGE, category,
                null, "original/cross-x.png", "file.png", "image/png",
                1000L, 100, null, LicenseType.PERSONAL
        ));
        Asset assetOwnedByY = assetRepository.save(Asset.register(
                userY, "Y 소유 콘텐츠", null, AssetType.IMAGE, category,
                null, "original/cross-y.png", "file.png", "image/png",
                1000L, 100, null, LicenseType.PERSONAL
        ));
        assetOwnedByXId = assetOwnedByX.getId();
        assetOwnedByYId = assetOwnedByY.getId();
    }

    @AfterEach
    void tearDown() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            entityManager.createNativeQuery("DELETE FROM creator_settlement WHERE creator_id IN (:x, :y)")
                    .setParameter("x", userXId).setParameter("y", userYId)
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM purchase WHERE user_id IN (:x, :y)")
                    .setParameter("x", userXId).setParameter("y", userYId)
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM credit_transaction WHERE user_id IN (:x, :y)")
                    .setParameter("x", userXId).setParameter("y", userYId)
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM asset WHERE id IN (:assetX, :assetY)")
                    .setParameter("assetX", assetOwnedByXId).setParameter("assetY", assetOwnedByYId)
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM `user` WHERE id IN (:x, :y)")
                    .setParameter("x", userXId).setParameter("y", userYId)
                    .executeUpdate();
        });
    }

    @Test
    void 서로의_콘텐츠를_동시에_구매해도_교착_없이_둘_다_처리된다() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<PurchaseResponse> yBuysFromX = () -> {
            ready.countDown();
            start.await();
            return purchaseService.purchase(userYId, new PurchaseRequest(assetOwnedByXId), "cross-1");
        };
        Callable<PurchaseResponse> xBuysFromY = () -> {
            ready.countDown();
            start.await();
            return purchaseService.purchase(userXId, new PurchaseRequest(assetOwnedByYId), "cross-2");
        };

        Future<PurchaseResponse> futureYBuysFromX = executor.submit(yBuysFromX);
        Future<PurchaseResponse> futureXBuysFromY = executor.submit(xBuysFromY);
        ready.await();
        start.countDown();

        List<PurchaseResponse> responses;
        try {
            responses = List.of(
                    futureYBuysFromX.get(10, TimeUnit.SECONDS),
                    futureXBuysFromY.get(10, TimeUnit.SECONDS)
            );
        } finally {
            executor.shutdown();
        }

        assertThat(responses).hasSize(2);
        User reloadedX = userRepository.findById(userXId).orElseThrow();
        User reloadedY = userRepository.findById(userYId).orElseThrow();
        assertThat(reloadedX.getCreditBalance()).isEqualTo(480);
        assertThat(reloadedY.getCreditBalance()).isEqualTo(480);
    }
}
