package com.example.bankcards.dto.response;

import com.example.bankcards.entity.enums.CardStatus;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

/**
 * DTO for returning card information to the client.
 */
public record CardResponse (
        UUID id,
        String maskedCardNumber,
        YearMonth expirationDate,
        CardStatus status,
        BigDecimal balance

){
}
