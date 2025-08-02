package com.example.bankcards.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A custom exception thrown when a requested resource is not found in the system.
 * <p>
 * This exception is annotated with {@code @ResponseStatus(HttpStatus.NOT_FOUND)},
 * which allows Spring's {@code ResponseStatusExceptionResolver} to automatically
 * handle it and return a 404 Not Found HTTP status to the client, even without
 * a custom {@code @ExceptionHandler}.
 */
@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException{
    /**
     * The name of the resource that was not found (e.g., "User", "Card").
     */
    private final String resourceName;

    /**
     * The name of the field used for the search (e.g., "id", "username").
     */
    private final String fieldName;

    /**
     * The value of the field used for the search.
     */
    private final transient Object fieldValue;

    /**
     * Constructs a new ResourceNotFoundException with a formatted message.
     *
     * @param resourceName The name of the resource.
     * @param fieldName    The name of the field.
     * @param fieldValue   The value of the field.
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * A simpler constructor for cases where a generic message is sufficient.
     *
     * @param message The detail message.
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }
}
