package com.example.aiverse.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 상품 관리 API는 MVP 범위에 없다 — 목록은 Flyway seed로만 관리하고 이 Entity는 조회 전용이다.
@Entity
@Table(name = "credit_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "credit_amount", nullable = false)
    private int creditAmount;

    @Column(name = "bonus_credit", nullable = false)
    private int bonusCredit;

    @Column(nullable = false)
    private int price;

    @Column(name = "display_order", nullable = false, unique = true)
    private int displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CreditProductStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public int totalCredit() {
        return creditAmount + bonusCredit;
    }
}
