package com.example.bankcards.repository;

import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
/**
 * Repository interface for RefreshToken entity.
 */
@Repository
public interface  RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    /**
     * Finds a refresh token by its token string.
     *
     * @param token The refresh token string to search for.
     * @return an Optional containing the found refresh token.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Deletes all refresh tokens associated with a specific user.
     * This is useful when a user logs out from all devices or when their account is disabled.
     * The @Modifying annotation is required for queries that change data.
     *
     * @param userId The ID of user whose refresh tokens should be deleted.
     * @return The number of tokens deleted.
     */
    @Modifying
    int deleteByUserId(UUID userId);

}
