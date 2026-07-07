package com.example.aiverse.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.aiverse.entity.Payment;
import com.example.aiverse.repository.PaymentRepository;
import com.example.aiverse.repository.jpa.PaymentJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey) {
        return paymentJpaRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
    }
}
