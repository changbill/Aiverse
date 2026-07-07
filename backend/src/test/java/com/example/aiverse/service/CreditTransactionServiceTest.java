package com.example.aiverse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.example.aiverse.common.response.PageResponse;
import com.example.aiverse.dto.CreditTransactionResponse;
import com.example.aiverse.entity.CreditTransaction;
import com.example.aiverse.entity.CreditTransactionType;
import com.example.aiverse.entity.User;
import com.example.aiverse.repository.CreditTransactionRepository;

@ExtendWith(MockitoExtension.class)
class CreditTransactionServiceTest {

    @Mock
    private CreditTransactionRepository creditTransactionRepository;

    private CreditTransactionService creditTransactionService;

    @BeforeEach
    void setUp() {
        creditTransactionService = new CreditTransactionService(creditTransactionRepository);
    }

    @Test
    void 유형_필터_없이_사용자_거래_이력을_조회한다() {
        User user = User.register("ledger@example.com", "encoded-password", "이력유저");
        CreditTransaction transaction = CreditTransaction.of(user, null, CreditTransactionType.CHARGE, 1100, 1100, "credit_product:PLUS");
        given(creditTransactionRepository.search(eq(5L), isNull(), any()))
                .willReturn(new PageImpl<>(java.util.List.of(transaction), PageRequest.of(0, 20), 1));

        PageResponse<CreditTransactionResponse> result = creditTransactionService.search(5L, null, 0, 20);

        assertThat(result.data()).extracting(CreditTransactionResponse::type).containsExactly(CreditTransactionType.CHARGE);
        assertThat(result.page().totalElements()).isEqualTo(1);
    }
}
