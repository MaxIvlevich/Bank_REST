package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Identifier cannot be blank")
        String identifier,

        @NotBlank(message = "Password cannot be blank")
        String password
) {
}
