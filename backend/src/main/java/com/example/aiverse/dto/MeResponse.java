package com.example.aiverse.dto;

import java.time.LocalDateTime;

import com.example.aiverse.entity.User;
import com.example.aiverse.entity.UserRole;
import com.example.aiverse.entity.UserStatus;

public record MeResponse(
        Long id,
        String email,
        String nickname,
        UserRole role,
        UserStatus status,
        String profileUrl,
        String introduction,
        int creditBalance,
        LocalDateTime createdAt
) {

    public static MeResponse from(User user) {
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getStatus(),
                user.getProfileUrl(),
                user.getIntroduction(),
                user.getCreditBalance(),
                user.getCreatedAt()
        );
    }
}
