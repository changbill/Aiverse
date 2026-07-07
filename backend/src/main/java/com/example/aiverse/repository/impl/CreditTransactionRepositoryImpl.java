package com.example.aiverse.repository.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.aiverse.entity.CreditTransaction;
import com.example.aiverse.entity.CreditTransactionType;
import com.example.aiverse.repository.CreditTransactionRepository;
import com.example.aiverse.repository.jpa.CreditTransactionJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CreditTransactionRepositoryImpl implements CreditTransactionRepository {

    private final CreditTransactionJpaRepository creditTransactionJpaRepository;

    @Override
    public CreditTransaction save(CreditTransaction creditTransaction) {
        return creditTransactionJpaRepository.save(creditTransaction);
    }

    @Override
    public Optional<CreditTransaction> findById(Long id) {
        return creditTransactionJpaRepository.findById(id);
    }

    @Override
    public Optional<CreditTransaction> findByPaymentId(Long paymentId) {
        return creditTransactionJpaRepository.findByPaymentId(paymentId);
    }

    @Override
    public Page<CreditTransaction> search(Long userId, CreditTransactionType type, Pageable pageable) {
        return creditTransactionJpaRepository.search(userId, type, pageable);
    }
}
