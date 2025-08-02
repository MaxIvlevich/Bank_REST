package com.example.bankcards.service.impl;

import com.example.bankcards.dto.LockedCards;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service("userCardService")
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> findMyCards(UUID userId, Pageable pageable) {
        log.info("Fetching active cards for user with ID: {}", userId);
        Page<Card> cards = cardRepository.findAllByOwnerIdAndActiveTrue(userId, pageable);
        return cards.map(cardMapper::toCardResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CardResponse findMyCardById(UUID cardId, UUID userId) {
        log.info("Fetching active card with ID: {} for user with ID: {}", cardId, userId);
        Card card = cardRepository.findByIdAndOwnerIdAndActiveTrue(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active card", "id", cardId));
        return cardMapper.toCardResponse(card);
    }

    @Override
    @Transactional
    public CardResponse requestCardBlock(UUID cardId, UUID userId) {
        log.info("User {} is requesting to block card {}", userId, cardId);
        Card card = cardRepository.findByIdAndOwnerIdAndActiveTrue(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active card", "id", cardId));

        if (card.getStatus() != CardStatus.ACTIVE) {
            log.warn("User {} tried to request a block for a card with status {}. Action denied.", userId, card.getStatus());
            throw new IllegalStateException("Card cannot be blocked because it is not in ACTIVE status. Current status: " + card.getStatus());
        }

        card.setStatus(CardStatus.BLOCK_REQUESTED);
        Card savedCard = cardRepository.save(card);
        log.info("Block request for card {} has been accepted. New status: {}", cardId, savedCard.getStatus());

        return cardMapper.toCardResponse(savedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMyCardBalance(UUID cardId, UUID userId) {
        log.info("Fetching balance for active card {} for user {}", cardId, userId);
        Card card = cardRepository.findByIdAndOwnerIdAndActiveTrue(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active card", "id", cardId));
        return card.getBalance();
    }

    @Override
    @Transactional
    public void transferBetweenMyCards(TransferRequest request, UUID userId) {
        log.info("User {} initiating transfer from card {} to card {}. Amount: {}",
                userId, request.fromCardId(), request.toCardId(), request.amount());

        LockedCards cards = findAndLockActiveCardsForTransfer(request.fromCardId(), request.toCardId());
        validateTransfer(request, cards.fromCard(), cards.toCard(), userId);
        executeTransfer(request.amount(), cards.fromCard(), cards.toCard());

        log.info("Transfer completed successfully for user {}", userId);
    }

    /**
     * Finds and applies a pessimistic lock on both ACTIVE cards involved in a transfer.
     */
    private LockedCards findAndLockActiveCardsForTransfer(UUID fromCardId, UUID toCardId) {
        Card fromCard = cardRepository.findActiveByIdWithLock(fromCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Active source card", "id", fromCardId));
        Card toCard = cardRepository.findActiveByIdWithLock(toCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Active destination card", "id", toCardId));
        return new LockedCards(fromCard, toCard);
    }

    /**
     * Validates a transfer operation based on business rules for a user's own cards.
     */
    private void validateTransfer(TransferRequest request, Card fromCard, Card toCard, UUID userId) {
        if (fromCard.getId().equals(toCard.getId())) {
            throw new IllegalArgumentException("Source and destination cards cannot be the same.");
        }

        if (!fromCard.getOwner().getId().equals(userId) || !toCard.getOwner().getId().equals(userId)) {
            throw new SecurityException("User does not own one or both of the cards involved in the transfer.");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Source card is not active.");
        }

        if (fromCard.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalStateException("Insufficient funds on the source card.");
        }
    }

    /**
     * Executes the actual balance change and saves the cards.
     */
    private void executeTransfer(BigDecimal amount, Card fromCard, Card toCard) {
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));
        cardRepository.saveAll(List.of(fromCard, toCard));
    }

}
