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
@Table(name = "creator_settlement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreatorSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "gross_credit", nullable = false)
    private int grossCredit;

    @Column(name = "platform_fee_credit", nullable = false)
    private int platformFeeCredit;

    @Column(name = "settlement_credit", nullable = false)
    private int settlementCredit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Builder
    private CreatorSettlement(
            User creator, Purchase purchase, Asset asset,
            int grossCredit, int platformFeeCredit, int settlementCredit
    ) {
        this.creator = creator;
        this.purchase = purchase;
        this.asset = asset;
        this.grossCredit = grossCredit;
        this.platformFeeCredit = platformFeeCredit;
        this.settlementCredit = settlementCredit;
        this.status = SettlementStatus.SETTLED;
        this.settledAt = LocalDateTime.now();
    }

    public static CreatorSettlement settle(
            User creator, Purchase purchase, Asset asset,
            int grossCredit, int platformFeeCredit, int settlementCredit
    ) {
        return CreatorSettlement.builder()
                .creator(creator)
                .purchase(purchase)
                .asset(asset)
                .grossCredit(grossCredit)
                .platformFeeCredit(platformFeeCredit)
                .settlementCredit(settlementCredit)
                .build();
    }
}
