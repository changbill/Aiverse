package com.example.aiverse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.example.aiverse.entity.CreditProduct;
import com.example.aiverse.entity.CreditTransaction;
import com.example.aiverse.entity.CreditTransactionType;
import com.example.aiverse.entity.Payment;
import com.example.aiverse.entity.User;
import com.example.aiverse.support.RepositoryIntegrationTestSupport;

import jakarta.persistence.EntityManager;

class CreditTransactionRepositoryTest extends RepositoryIntegrationTestSupport {

    @Autowired
    private CreditTransactionRepository creditTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 사용자의_거래_이력을_유형별로_필터링해_최신순으로_조회한다() {
        User user = userRepository.save(User.register("ledger@example.com", "encoded-password", "이력유저"));
        CreditProduct product = entityManager.find(CreditProduct.class, 1L);
        Payment payment = Payment.mockSuccess(user, product, "idem-key-2");
        entityManager.persist(payment);
        creditTransactionRepository.save(CreditTransaction.of(user, payment, CreditTransactionType.CHARGE, 500, 500, "credit_product:BASIC"));
        creditTransactionRepository.save(CreditTransaction.of(user, null, CreditTransactionType.PURCHASE, -100, 400, "purchase:1"));

        var chargeOnly = creditTransactionRepository.search(user.getId(), CreditTransactionType.CHARGE, PageRequest.of(0, 10));
        var all = creditTransactionRepository.search(user.getId(), null, PageRequest.of(0, 10));

        assertThat(chargeOnly.getTotalElements()).isEqualTo(1);
        assertThat(chargeOnly.getContent().getFirst().getType()).isEqualTo(CreditTransactionType.CHARGE);
        assertThat(all.getTotalElements()).isEqualTo(2);
        assertThat(all.getContent().getFirst().getType()).isEqualTo(CreditTransactionType.PURCHASE);
    }
}
