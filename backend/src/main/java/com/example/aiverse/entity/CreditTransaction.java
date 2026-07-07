package com.example.aiverse.entity;

import java.time.LocalDateTime;

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
@Table(name = "credit_transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CreditTransactionType type;

    @Column(nullable = false)
    private int amount;

    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    @Column(nullable = false, length = 500)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private CreditTransaction(
            User user, Payment payment, CreditTransactionType type,
            int amount, int balanceAfter, String reason
    ) {
        this.user = user;
        this.payment = payment;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.reason = reason;
    }

    public static CreditTransaction of(
            User user, Payment payment, CreditTransactionType type,
            int amount, int balanceAfter, String reason
    ) {
        return CreditTransaction.builder()
                .user(user)
                .payment(payment)
                .type(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .reason(reason)
                .build();
    }
}
