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

import com.example.aiverse.dto.PaymentRequest;
import com.example.aiverse.entity.User;
import com.example.aiverse.repository.UserRepository;
import com.example.aiverse.support.IntegrationTestSupport;

import jakarta.persistence.EntityManager;

/**
 * 동시 결제 요청에서 비관적 쓰기 잠금과 Idempotency-Key 재요청 처리가 실제로
 * 여러 커넥션 간에 직렬화되는지 검증한다. 각 스레드가 독립된 트랜잭션으로
 * 커밋해야 하므로 클래스 전체를 상위 롤백 트랜잭션에서 제외한다.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PaymentConcurrencyTest extends IntegrationTestSupport {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long userId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(
                User.register("concurrency-" + UUID.randomUUID() + "@example.com", "encoded-password", "동시성유저" + System.nanoTime() % 100000)
        );
        userId = user.getId();
    }

    @AfterEach
    void tearDown() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            entityManager.createNativeQuery("DELETE FROM credit_transaction WHERE user_id = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM payment WHERE user_id = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
            entityManager.createNativeQuery("DELETE FROM `user` WHERE id = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
        });
    }

    @Test
    void 서로_다른_Idempotency_Key로_동시_충전하면_두_결제_모두_반영된다() throws Exception {
        var responses = runConcurrently("concurrent-key-A", "concurrent-key-B");

        User reloaded = userRepository.findById(userId).orElseThrow();
        int expectedBalance = responses.get(0).grantedCredit() + responses.get(1).grantedCredit();
        assertThat(reloaded.getCreditBalance()).isEqualTo(expectedBalance);
        assertThat(responses.get(0).paymentId()).isNotEqualTo(responses.get(1).paymentId());
    }

    @Test
    void 같은_Idempotency_Key로_동시_충전해도_한_번만_반영된다() throws Exception {
        var responses = runConcurrently("same-key", "same-key");

        User reloaded = userRepository.findById(userId).orElseThrow();
        assertThat(reloaded.getCreditBalance()).isEqualTo(responses.get(0).grantedCredit());
        assertThat(responses.get(0).paymentId()).isEqualTo(responses.get(1).paymentId());
    }

    private List<com.example.aiverse.dto.PaymentResponse> runConcurrently(String idempotencyKeyA, String idempotencyKeyB) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<com.example.aiverse.dto.PaymentResponse> taskA = chargeTask(idempotencyKeyA, ready, start);
        Callable<com.example.aiverse.dto.PaymentResponse> taskB = chargeTask(idempotencyKeyB, ready, start);

        Future<com.example.aiverse.dto.PaymentResponse> futureA = executor.submit(taskA);
        Future<com.example.aiverse.dto.PaymentResponse> futureB = executor.submit(taskB);
        ready.await();
        start.countDown();

        try {
            return List.of(futureA.get(10, TimeUnit.SECONDS), futureB.get(10, TimeUnit.SECONDS));
        } finally {
            executor.shutdown();
        }
    }

    private Callable<com.example.aiverse.dto.PaymentResponse> chargeTask(String idempotencyKey, CountDownLatch ready, CountDownLatch start) {
        return () -> {
            ready.countDown();
            start.await();
            return paymentService.charge(userId, new PaymentRequest(1L), idempotencyKey);
        };
    }
}
