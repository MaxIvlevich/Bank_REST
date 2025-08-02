package com.example.bankcards.service;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service for user-specific card operations.
 * This service operates only on the cards owned by the current user and ensures
 * that only active (not soft-deleted) cards are accessible.
 */
public interface CardService {

    /**
     * Finds a paginated list of active cards for a specific user.
     *
     * @param userId   The ID of the user whose cards to find.
     * @param pageable Pagination information.
     * @return A page of card DTOs.
     */
    Page<CardResponse> findMyCards(UUID userId, Pageable pageable);

    /**
     * Finds a single active card by its ID, ensuring it belongs to the specified user.
     *
     * @param cardId The ID of the card to find.
     * @param userId The ID of the user who must own the card.
     * @return A DTO with the card's information.
     */
    CardResponse findMyCardById(UUID cardId, UUID userId);

    /**
     * Initiates a request from a user to block their own active card.
     * This changes the card's status to BLOCK_REQUESTED, pending admin approval.
     *
     * @param cardId The ID of the card to block.
     * @param userId The ID of the user requesting the block.
     * @return An updated DTO of the card with the new status.
     */
    CardResponse requestCardBlock(UUID cardId, UUID userId);

    /**
     * Retrieves the balance for a specific active card, ensuring user ownership.
     *
     * @param cardId The ID of the card.
     * @param userId The ID of the owning user.
     * @return The current balance of the card.
     */
    BigDecimal getMyCardBalance(UUID cardId, UUID userId);

    /**
     * Performs a money transfer between two active cards belonging to the same user.
     *
     * @param request DTO containing transfer details (fromCardId, toCardId, amount).
     * @param userId  The ID of the user performing the transfer.
     */
    void transferBetweenMyCards(TransferRequest request, UUID userId);
}
