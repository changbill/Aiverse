package com.example.aiverse.repository.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.aiverse.entity.RefreshToken;
import com.example.aiverse.repository.RefreshTokenRepository;
import com.example.aiverse.repository.jpa.RefreshTokenJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenJpaRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenJpaRepository.findByTokenHash(tokenHash);
    }

    @Override
    public List<RefreshToken> findActiveByUserId(Long userId, LocalDateTime now) {
        return refreshTokenJpaRepository.findByUserIdAndRevokedAtIsNullAndExpiresAtAfter(userId, now);
    }
}
