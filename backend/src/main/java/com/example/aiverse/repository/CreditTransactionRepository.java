package com.example.aiverse.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.aiverse.entity.CreditTransaction;
import com.example.aiverse.entity.CreditTransactionType;

public interface CreditTransactionRepository {

    CreditTransaction save(CreditTransaction creditTransaction);

    Page<CreditTransaction> search(Long userId, CreditTransactionType type, Pageable pageable);
}
