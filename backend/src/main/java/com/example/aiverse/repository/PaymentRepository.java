package com.example.aiverse.repository;

import java.util.Optional;

import com.example.aiverse.entity.Payment;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
}
