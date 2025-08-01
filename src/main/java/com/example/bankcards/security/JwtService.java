package com.example.bankcards.security;

import org.springframework.security.core.Authentication;

/**
 * Service interface for JSON Web Token (JWT) operations.
 * Defines the contract for generating, validating, and parsing tokens.
 */
public interface JwtService {
    /**
     * Generates a new JWT access token for the given authenticated user.
     *
     * @param authentication The authentication object containing user details.
     * @return A string representation of the JWT.
     */
    String generateAccessToken(Authentication authentication);

    /**
     * Extracts the username from a given JWT.
     *
     * @param token The JWT string.
     * @return The username (subject) from the token.
     */
    String getUsernameFromToken(String token);

    /**
     * Validates the structure and signature of a JWT.
     * Checks for expiration, malformation, and signature validity.
     *
     * @param token The JWT string to validate.
     * @return true if the token is valid, false otherwise.
     */
    boolean isTokenValid(String token);
}
