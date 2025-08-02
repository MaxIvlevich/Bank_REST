package com.example.bankcards.service.impl;

import com.example.bankcards.dto.LockedCards;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidOperationException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UnauthorizedOperationException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.query.CardQueryService;
import com.example.bankcards.mapper.CardMapper;
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
    private final CardQueryService cardQueryService;

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> findMyCards(UUID userId, Pageable pageable) {
        log.info("FIND_MY_CARDS: [userId={}].", userId);
        Page<Card> cards = cardRepository.findAllByOwnerIdAndActiveTrue(userId, pageable);
        return cards.map(cardMapper::toCardResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CardResponse findMyCardById(UUID cardId, UUID userId) {
        log.info("FIND_MY_CARD_BY_ID: [userId={}, cardId={}].", userId, cardId);
        Card card = cardQueryService.findActiveByIdAndOwnerOrThrow(cardId, userId);
        return cardMapper.toCardResponse(card);
    }

    @Override
    @Transactional
    public CardResponse requestCardBlock(UUID cardId, UUID userId) {
        log.info("REQUEST_CARD_BLOCK: [userId={}, cardId={}].", userId, cardId);
        Card card = cardQueryService.findActiveByIdAndOwnerOrThrow(cardId, userId);

        if (card.getStatus() != CardStatus.ACTIVE) {
            log.warn("REQUEST_CARD_BLOCK_FAIL: [userId={}, cardId={}]. Reason: Card is not in ACTIVE status. Current status: {}.",
                    userId, card.getId(), card.getStatus());
            throw new InvalidOperationException("Card cannot be blocked because it is not in ACTIVE status. Current status: " + card.getStatus());
        }

        card.setStatus(CardStatus.BLOCK_REQUESTED);
        Card savedCard = cardRepository.save(card);
        log.info("REQUEST_CARD_BLOCK_SUCCESS: [cardId={}]. New status: {}", cardId, savedCard.getStatus());

        return cardMapper.toCardResponse(savedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMyCardBalance(UUID cardId, UUID userId) {
        log.info("GET_MY_CARD_BALANCE: [userId={}, cardId={}].", userId, cardId);
        Card card = cardRepository.findByIdAndOwnerIdAndActiveTrue(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active card", "id", cardId));
        return card.getBalance();
    }

    @Override
    @Transactional
    public void transferBetweenMyCards(TransferRequest request, UUID userId) {
        log.info("TRANSFER_START: [userId={}, fromCardId={}, toCardId={}, amount={}].",
                userId, request.fromCardId(), request.toCardId(), request.amount());

        LockedCards cards = findAndLockActiveCardsForTransfer(request.fromCardId(), request.toCardId());
        validateTransfer(request, cards.fromCard(), cards.toCard(), userId);
        executeTransfer(request.amount(), cards.fromCard(), cards.toCard());

        log.info("TRANSFER_SUCCESS: [userId={}].", userId);
    }

    /**
     * Finds and applies a pessimistic lock on both ACTIVE cards involved in a transfer.
     */
    private LockedCards findAndLockActiveCardsForTransfer(UUID fromCardId, UUID toCardId) {
        Card fromCard = cardQueryService.findActiveByIdWithLockOrThrow(fromCardId);
        Card toCard =  cardQueryService.findActiveByIdWithLockOrThrow(toCardId);
        return new LockedCards(fromCard, toCard);
    }

    /**
     * Validates a transfer operation based on business rules for a user's own cards.
     */
    private void validateTransfer(TransferRequest request, Card fromCard, Card toCard, UUID userId) {
        if (fromCard.getId().equals(toCard.getId())) {
            throw new InvalidOperationException("Source and destination cards cannot be the same.");
        }

        if (!fromCard.getOwner().getId().equals(userId) || !toCard.getOwner().getId().equals(userId)) {
            throw new UnauthorizedOperationException("User does not own one or both of the cards involved in the transfer.");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidOperationException("Source card is not active.");
        }

        if (fromCard.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(fromCard.getBalance(), request.amount());
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
