package com.example.bankcards.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

/**
 * DTO for creating a new bank card.
 */
public record CreateCardRequest (
        @NotNull(message = "Owner ID cannot be null")
        UUID ownerId,

        @NotBlank(message = "Card number cannot be blank")
        @Pattern(regexp = "^\\d{16}$", message = "Card number must be exactly 16 digits")
        String cardNumber,

        @NotNull(message = "Expiration date cannot be null")
        @Future(message = "Expiration date must be in the future")
        YearMonth expirationDate,

        @DecimalMin(value = "0.0",  message = "Initial balance cannot be negative")
        BigDecimal initialBalance

){
}
