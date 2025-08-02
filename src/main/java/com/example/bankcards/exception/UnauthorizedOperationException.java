package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Thrown when an authenticated user attempts to perform an operation
 * on a resource they do not own or are not authorized to access.
 * <p>
 * Maps to HTTP 403 Forbidden.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedOperationException extends RuntimeException{

    public UnauthorizedOperationException(String message) {
        super(message);
    }

    public UnauthorizedOperationException(UUID userId, UUID resourceId) {
        super(String.format("User with id [%s] is not authorized for resource with id [%s]", userId, resourceId));
    }
}
