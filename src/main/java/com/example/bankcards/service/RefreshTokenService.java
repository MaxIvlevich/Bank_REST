package com.example.bankcards.service;

import com.example.bankcards.entity.RefreshToken;

import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing refresh tokens.
 * Defines the contract for creating, retrieving, and verifying refresh tokens.
 */
public interface RefreshTokenService {
    /**
     * Finds a refresh token by its token string.
     *
     * @param token The token string.
     * @return An Optional containing the RefreshToken entity.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Creates and persists a new refresh token for a given user.
     * If a token for the user already exists, it will be replaced.
     *
     * @param userId The ID of the user for whom the token is created.
     * @return The created RefreshToken entity.
     */
    RefreshToken createRefreshToken(UUID userId);

    /**
     * Verifies if a refresh token has expired.
     *
     * @param token The RefreshToken entity to check.
     * @return The same RefreshToken entity if it's not expired.
     * @throws RuntimeException (or a custom exception) if the token is expired.
     */
    RefreshToken verifyExpiration(RefreshToken token);

    /**
     * Deletes the refresh token associated with a user.
     * Useful for logout functionality.
     *
     * @param userId The ID of the user whose token should be deleted.
     */
    void deleteByUserId(UUID userId);
}
