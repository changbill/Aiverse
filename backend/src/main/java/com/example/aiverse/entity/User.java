package com.example.aiverse.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 이메일 정규화, 비밀번호 해시, 닉네임 검증은 서비스 계층 책임 — 이 Entity는 이미 검증된 값만 저장한다.
@Entity
@Table(name = "`user`")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "profile_url", length = 2048)
    private String profileUrl;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "credit_balance", nullable = false)
    private int creditBalance;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private User(String email, String password, String nickname, UserRole role, UserStatus status,
            String profileUrl, String introduction, int creditBalance) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role != null ? role : UserRole.USER;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.profileUrl = profileUrl;
        this.introduction = introduction;
        this.creditBalance = creditBalance;
    }

    public static User register(String email, String password, String nickname) {
        return User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .creditBalance(0)
                .build();
    }

    // null인 파라미터는 기존 값을 유지한다. email·password·role·creditBalance는 이 메서드로 변경할 수 없다.
    public void updateProfile(String nickname, String profileUrl, String introduction) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (profileUrl != null) {
            this.profileUrl = profileUrl;
        }
        if (introduction != null) {
            this.introduction = introduction;
        }
    }

    // 잔액 잠금(비관적 쓰기 잠금)은 Repository 조회 시점 책임 — 이 메서드는 이미 잠긴 행의 계산만 수행한다.
    public void increaseCredit(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 크레딧은 0보다 커야 합니다.");
        }
        this.creditBalance += amount;
    }

    public void decreaseCredit(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("차감 크레딧은 0보다 커야 합니다.");
        }
        if (this.creditBalance < amount) {
            throw new IllegalStateException("크레딧 잔액이 부족합니다.");
        }
        this.creditBalance -= amount;
    }
}
