package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
        @NotBlank
        String refreshToken
) {
}
