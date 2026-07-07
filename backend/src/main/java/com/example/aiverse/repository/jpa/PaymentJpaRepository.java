package com.example.aiverse.repository.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aiverse.entity.Payment;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
}
