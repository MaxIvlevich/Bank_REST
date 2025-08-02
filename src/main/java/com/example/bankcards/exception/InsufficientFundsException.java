package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

/**
 * Thrown when a financial operation (e.g., a transfer) fails due to
 * insufficient funds in the source account or card.
 * <p>
 * Maps to HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientFundsException extends RuntimeException{

    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(BigDecimal available, BigDecimal required) {
        super(String.format("Insufficient funds. Required: %s, Available: %s", required, available));
    }
}
