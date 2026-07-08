package com.example.aiverse.dto;

import jakarta.validation.constraints.Size;

public record MeUpdateRequest(
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
        String nickname,

        String profileUrl,

        String introduction
) {
}
