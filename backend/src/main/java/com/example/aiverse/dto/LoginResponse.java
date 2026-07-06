package com.example.aiverse.dto;

import com.example.aiverse.entity.User;
import com.example.aiverse.entity.UserRole;

public record LoginResponse(String accessToken, UserSummary user) {

    public static LoginResponse of(String accessToken, User user) {
        return new LoginResponse(accessToken, UserSummary.from(user));
    }

    public record UserSummary(Long id, String email, String nickname, UserRole role, int creditBalance) {

        public static UserSummary from(User user) {
            return new UserSummary(
                    user.getId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getRole(),
                    user.getCreditBalance()
            );
        }
    }
}
