package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an operation cannot be completed because it violates a business rule
 * or the system is in an inappropriate state for the requested operation.
 * <p>
 * Maps to HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOperationException  extends RuntimeException{

    public InvalidOperationException(String message) {
        super(message);
    }
}
