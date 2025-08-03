package com.example.bankcards.repository;

import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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
     * Deletes a refresh token by the user's ID using a custom JPQL query.
     * This is more efficient than a derived delete query.
     *
     * @param userId The ID of the user whose token should be deleted.
     */
    @Modifying // Обязательно для изменяющих запросов
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    int deleteByUserId(UUID userId);

}
