package com.example.aiverse.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.aiverse.entity.CreditTransaction;
import com.example.aiverse.entity.CreditTransactionType;

public interface CreditTransactionRepository {

    CreditTransaction save(CreditTransaction creditTransaction);

    Optional<CreditTransaction> findByPaymentId(Long paymentId);

    Page<CreditTransaction> search(Long userId, CreditTransactionType type, Pageable pageable);
}
