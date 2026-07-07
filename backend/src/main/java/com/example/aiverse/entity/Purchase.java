package com.example.aiverse.entity;

import java.time.LocalDateTime;

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
@Table(name = "purchase")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "credit_transaction_id", nullable = false)
    private Long creditTransactionId;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "purchase_price_credit", nullable = false)
    private int purchasePriceCredit;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_type", nullable = false, length = 20)
    private LicenseType licenseType;

    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;

    @Builder
    private Purchase(
            User user, Asset asset, Long creditTransactionId, String idempotencyKey,
            int purchasePriceCredit, LicenseType licenseType
    ) {
        this.user = user;
        this.asset = asset;
        this.creditTransactionId = creditTransactionId;
        this.idempotencyKey = idempotencyKey;
        this.purchasePriceCredit = purchasePriceCredit;
        this.licenseType = licenseType;
        this.purchasedAt = LocalDateTime.now();
    }

    public static Purchase of(
            User user, Asset asset, Long creditTransactionId, String idempotencyKey,
            int purchasePriceCredit, LicenseType licenseType
    ) {
        return Purchase.builder()
                .user(user)
                .asset(asset)
                .creditTransactionId(creditTransactionId)
                .idempotencyKey(idempotencyKey)
                .purchasePriceCredit(purchasePriceCredit)
                .licenseType(licenseType)
                .build();
    }
}
