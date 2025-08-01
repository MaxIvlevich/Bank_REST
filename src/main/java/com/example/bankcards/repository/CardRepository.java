package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface CardRepository  extends JpaRepository<Card, UUID> {

    /**
     * Finds a card by its ID and the owner's ID.
     * This ensures that a user can only access their own cards.
     *
     * @param id      The ID of the card.
     * @param ownerId The ID of the user who owns the card.
     * @return an Optional containing the found card.
     */
    Optional<Card> findByIdAndOwnerId(UUID id, UUID ownerId);

    /**
     * Finds all cards belonging to a specific user, with pagination.
     * The Pageable parameter allows for sorting, and specifying page number and size.
     *
     * @param ownerId  The ID of the user whose cards to find.
     * @param pageable The pagination information.
     * @return a Page of cards.
     */
    Page<Card> findAllByOwnerId(UUID ownerId, Pageable pageable);

    /**
     * Checks if a card with the given card number already exists.
     * Useful for ensuring card numbers are unique before creating a new card.
     *
     * @param cardNumber The card number to check.
     * @return true if a card with this number exists, false otherwise.
     */
    boolean existsByCardNumber(String cardNumber);

    /**
     * Finds a card by its ID and applies a pessimistic write lock.
     * This method should be used within a @Transactional context
     * when the card's balance is about to be modified to prevent race conditions.
     *
     * @param id The ID of the card to find and lock.
     * @return an Optional containing the locked card.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Card c where c.id = :id")
    Optional<Card> findAndLockById(@Param("id") UUID id);

}
