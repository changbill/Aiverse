package com.example.aiverse.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.aiverse.entity.RefreshToken;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findActiveByUserId(Long userId, LocalDateTime now);
}
