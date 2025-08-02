package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A custom exception thrown when an error occurs during the token refresh process.
 * This typically happens when the provided refresh token is expired, invalid, or not found.
 * <p>
 * This exception is mapped to an HTTP 403 Forbidden status, indicating that the
 * server understood the request but refuses to authorize it. This is often more
 * appropriate than 401 Unauthorized for an invalid refresh token, as the initial
 * authentication (the refresh token itself) is being rejected.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenRefreshException  extends RuntimeException{
    /**
     * Constructs a new TokenRefreshException with the specified detail message.
     *
     * @param message The detail message.
     */
    public TokenRefreshException(String message) {
        super(message);
    }

    /**
     * Constructs a new TokenRefreshException with a formatted message
     * including the problematic token.
     *
     * @param token   The refresh token that caused the error.
     * @param message The detail message explaining the reason.
     */
    public TokenRefreshException(String token, String message) {
        super(String.format("Failed for token [%s]: %s", token, message));
    }
}
