package com.example.bankcards.service.query;

import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * A specialized service for querying Card entities.
 * Provides methods for both user and admin contexts.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardQueryService {

    private final CardRepository cardRepository;



    /**
     * Finds any card by its ID or throws a ResourceNotFoundException.
     *
     * @param cardId The ID of the card to find.
     * @return The found Card entity.
     */
    public Card findByIdOrThrow(UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId));
    }


    /**
     * Finds an active card by its ID and owner ID, or throws a ResourceNotFoundException.
     *
     * @param cardId The ID of the card to find.
     * @param userId The ID of the expected owner.
     * @return The found Card entity.
     */
    public Card findActiveByIdAndOwnerOrThrow(UUID cardId, UUID userId) {
        return cardRepository.findByIdAndOwnerIdAndActiveTrue(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active card for user", "id", cardId));
    }

    /**
     * Finds an active card by its ID, locks it, or throws a ResourceNotFoundException.
     *
     * @param cardId The ID of the card to find and lock.
     * @return The locked Card entity.
     */
    @Transactional
    public Card findActiveByIdWithLockOrThrow(UUID cardId) {
        return cardRepository.findActiveByIdWithLock(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Active card", "id", cardId));
    }
}
