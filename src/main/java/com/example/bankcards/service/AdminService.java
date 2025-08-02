package com.example.bankcards.service;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.UpdateUserRolesRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service for administrative operations.
 * This service provides methods for managing all users and cards in the system,
 * often bypassing standard user-level restrictions.
 */
public interface AdminService {
    // =========== Card Management ===========

    /**
     * Creates a new bank card for a specified user.
     *
     * @param request DTO containing card and owner information.
     * @return A DTO of the newly created card.
     */
    CardResponse createCard(CreateCardRequest request);

    /**
     * Finds all cards in the system, including active and inactive (soft-deleted).
     *
     * @param pageable Pagination information.
     * @return A page of all card DTOs.
     */
    Page<CardResponse> findAllCards(Pageable pageable);

    /**
     * Finds all cards for a specific user, including inactive ones.
     *
     * @param userId The ID of the user.
     * @param pageable Pagination information.
     * @return A page of card DTOs for the specified user.
     */
    Page<CardResponse> findAllCardsByUserId(UUID userId, Pageable pageable);

    /**
     * Finds all cards with a specific status.
     *
     * @param status The status to filter by.
     * @param pageable Pagination information.
     * @return A page of card DTOs.
     */
    Page<CardResponse> findCardsByStatus(CardStatus status, Pageable pageable);

    /**
     * Activates a card.
     *
     * @param cardId The ID of the card to activate.
     * @return An updated DTO of the card.
     */
    CardResponse activateCard(UUID cardId);

    /**
     * Confirms a user's request to block a card.
     *
     * @param cardId The ID of the card to block.
     * @return An updated DTO of the card.
     */
    CardResponse confirmCardBlock(UUID cardId);

    /**
     * Declines a user's request to block a card.
     *
     * @param cardId The ID of the card to unblock.
     * @return An updated DTO of the card.
     */
    CardResponse declineCardBlock(UUID cardId);

    /**
     * Soft-deletes a card.
     *
     * @param cardId The ID of the card to delete.
     */
    void softDeleteCard(UUID cardId);

    // =========== User Management ===========

    /**
     * Finds a single user by their ID.
     * @param userId The ID of the user.
     * @return A DTO of the found user.
     */
    UserResponseDto findUserById(UUID userId);

    /**
     * Finds all users in the system.
     * @param pageable Pagination information.
     * @return A page of user DTOs.
     */
    Page<UserResponseDto> findAllUsers(Pageable pageable);

    /**
     * Updates the roles for a specific user.
     * @param userId The ID of the user to update.
     * @param request DTO containing the new set of roles.
     * @return An updated DTO of the user.
     */
    UserResponseDto updateUserRoles(UUID userId, UpdateUserRolesRequest request);

    /**
     * Locks a user's account (sets is_active to false).
     * @param userId The ID of the user to lock.
     * @return An updated DTO of the user.
     */
    UserResponseDto lockUserAccount(UUID userId);

    /**
     * Unlocks a user's account (sets is_active to true).
     * @param userId The ID of the user to unlock.
     * @return An updated DTO of the user.
     */
    UserResponseDto unlockUserAccount(UUID userId);
}
