package com.example.aiverse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.aiverse.entity.CreditProduct;
import com.example.aiverse.entity.Payment;
import com.example.aiverse.entity.User;
import com.example.aiverse.support.RepositoryIntegrationTestSupport;

import jakarta.persistence.EntityManager;

class PaymentRepositoryTest extends RepositoryIntegrationTestSupport {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 사용자와_Idempotency_Key로_결제를_조회할_수_있다() {
        User user = userRepository.save(User.register("payment@example.com", "encoded-password", "결제유저"));
        CreditProduct product = entityManager.find(CreditProduct.class, 2L);
        Payment payment = paymentRepository.save(Payment.mockSuccess(user, product, "idem-key-1"));

        assertThat(paymentRepository.findByUserIdAndIdempotencyKey(user.getId(), "idem-key-1"))
                .isPresent()
                .get()
                .extracting(Payment::getId)
                .isEqualTo(payment.getId());

        assertThat(paymentRepository.findByUserIdAndIdempotencyKey(user.getId(), "no-such-key")).isEmpty();
    }
}
