package com.example.aiverse.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "download")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Download {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @CreationTimestamp
    @Column(name = "downloaded_at", nullable = false, updatable = false)
    private LocalDateTime downloadedAt;

    @Builder
    private Download(Purchase purchase, User user, Asset asset) {
        this.purchase = purchase;
        this.user = user;
        this.asset = asset;
    }

    public static Download of(Purchase purchase, User user, Asset asset) {
        return Download.builder()
                .purchase(purchase)
                .user(user)
                .asset(asset)
                .build();
    }
}
