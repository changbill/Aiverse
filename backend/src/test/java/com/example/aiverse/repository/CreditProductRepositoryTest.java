package com.example.aiverse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.aiverse.support.RepositoryIntegrationTestSupport;

import jakarta.persistence.EntityManager;

class CreditProductRepositoryTest extends RepositoryIntegrationTestSupport {

    @Autowired
    private CreditProductRepository creditProductRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void ID로_크레딧_상품을_조회할_수_있다() {
        assertThat(creditProductRepository.findById(1L))
                .isPresent()
                .get()
                .extracting(product -> product.getCode())
                .isEqualTo("BASIC");
    }

    @Test
    void 활성_상품을_표시_순서대로_조회한다() {
        var products = creditProductRepository.findAllActiveOrderByDisplayOrder();

        assertThat(products).hasSize(3);
        assertThat(products).extracting(product -> product.getCode())
                .containsExactly("BASIC", "PLUS", "PRO");
    }

    @Test
    void 비활성_상품은_활성_목록에서_제외된다() {
        entityManager.createNativeQuery("UPDATE credit_product SET status = 'INACTIVE' WHERE id = 3")
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        var products = creditProductRepository.findAllActiveOrderByDisplayOrder();

        assertThat(products).hasSize(2);
        assertThat(products).extracting(product -> product.getCode())
                .containsExactly("BASIC", "PLUS");
    }
}
