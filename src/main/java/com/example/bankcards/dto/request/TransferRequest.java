package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull(message = "Source card ID cannot be null")
        UUID fromCardId,

        @NotNull(message = "Destination card ID cannot be null")
        UUID toCardId,

        @NotNull(message = "Amount cannot be null")
        @Positive(message = "Transfer amount must be positive")
        BigDecimal amount
) {
}
