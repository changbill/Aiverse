package com.example.aiverse.dto;

import java.time.LocalDateTime;

import com.example.aiverse.entity.User;
import com.example.aiverse.entity.UserRole;

public record RegisterResponse(
        Long id,
        String email,
        String nickname,
        UserRole role,
        int creditBalance,
        LocalDateTime createdAt
) {

    public static RegisterResponse from(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getCreditBalance(),
                user.getCreatedAt()
        );
    }
}
