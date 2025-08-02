package com.example.bankcards.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A custom exception thrown when an attempt is made to create a resource
 * that already exists, violating a uniqueness constraint (e.g., duplicate username or email).
 * <p>
 * This exception is mapped to an HTTP 409 Conflict status.
 */
@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException{
    /**
     * The name of the resource that is duplicated (e.g., "User", "Card").
     */
    private final String resourceName;

    /**
     * The name of the field that must be unique (e.g., "username", "cardNumber").
     */
    private final String fieldName;

    /**
     * The value of the field that caused the conflict.
     */
    private final transient Object fieldValue;

    /**
     * Constructs a new DuplicateResourceException with a formatted message.
     *
     * @param resourceName The name of the resource.
     * @param fieldName    The name of the unique field.
     * @param fieldValue   The value that already exists.
     */
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s with %s '%s' already exists.", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
