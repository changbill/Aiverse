package com.example.aiverse.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_product_id", nullable = false)
    private CreditProduct creditProduct;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "transaction_key")
    private String transactionKey;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "failed_reason", length = 500)
    private String failedReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Payment(
            User user, CreditProduct creditProduct, String idempotencyKey, int amount,
            PaymentMethod method, PaymentStatus status, String transactionKey, LocalDateTime paidAt
    ) {
        this.user = user;
        this.creditProduct = creditProduct;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.transactionKey = transactionKey;
        this.paidAt = paidAt;
    }

    public static Payment mockSuccess(User user, CreditProduct creditProduct, String idempotencyKey) {
        return Payment.builder()
                .user(user)
                .creditProduct(creditProduct)
                .idempotencyKey(idempotencyKey)
                .amount(creditProduct.getPrice())
                .method(PaymentMethod.MOCK)
                .status(PaymentStatus.SUCCESS)
                .transactionKey("mock-" + UUID.randomUUID())
                .paidAt(LocalDateTime.now())
                .build();
    }
}
