package com.example.bankcards.dto.response;

import java.math.BigDecimal;

public record TransactionResponse(
        BigDecimal fromCard,
        BigDecimal toCard
){
}
