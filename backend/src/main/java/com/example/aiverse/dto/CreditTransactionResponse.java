package com.example.aiverse.dto;

import java.time.LocalDateTime;

import com.example.aiverse.entity.CreditTransaction;
import com.example.aiverse.entity.CreditTransactionType;

public record CreditTransactionResponse(
        Long id, CreditTransactionType type, int amount, int balanceAfter, String reason, LocalDateTime createdAt
) {

    public static CreditTransactionResponse from(CreditTransaction creditTransaction) {
        return new CreditTransactionResponse(
                creditTransaction.getId(), creditTransaction.getType(), creditTransaction.getAmount(),
                creditTransaction.getBalanceAfter(), creditTransaction.getReason(), creditTransaction.getCreatedAt()
        );
    }
}
