package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        /**
         * Canonical constructor to normalize the amount.
         * This ensures that any amount passed to this DTO is set to a financial standard scale.
         */
        public TransferRequest {
                if (amount != null) {
                        amount = amount.setScale(2, RoundingMode.HALF_UP);
                }
        }
}
