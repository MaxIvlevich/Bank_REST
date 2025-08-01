package com.example.bankcards.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
        @NotBlank
        String refreshToken
) {
}
