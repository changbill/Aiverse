package com.example.aiverse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.aiverse.dto.CreditProductResponse;
import com.example.aiverse.entity.CreditProduct;
import com.example.aiverse.repository.CreditProductRepository;

@ExtendWith(MockitoExtension.class)
class CreditProductServiceTest {

    @Mock
    private CreditProductRepository creditProductRepository;

    private CreditProductService creditProductService;

    @BeforeEach
    void setUp() {
        creditProductService = new CreditProductService(creditProductRepository);
    }

    @Test
    void 활성_상품을_표시_순서대로_반환한다() {
        CreditProduct basic = product(1L, "BASIC", "Basic", 500, 0, 5000, 1);
        CreditProduct plus = product(2L, "PLUS", "Plus", 1000, 100, 10000, 2);
        given(creditProductRepository.findAllActiveOrderByDisplayOrder()).willReturn(List.of(basic, plus));

        List<CreditProductResponse> result = creditProductService.getActiveProducts();

        assertThat(result).extracting(CreditProductResponse::code).containsExactly("BASIC", "PLUS");
    }

    private CreditProduct product(Long id, String code, String name, int creditAmount, int bonusCredit, int price, int displayOrder) {
        try {
            var constructor = CreditProduct.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            CreditProduct product = constructor.newInstance();
            setField(product, "id", id);
            setField(product, "code", code);
            setField(product, "name", name);
            setField(product, "creditAmount", creditAmount);
            setField(product, "bonusCredit", bonusCredit);
            setField(product, "price", price);
            setField(product, "displayOrder", displayOrder);
            return product;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = CreditProduct.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
