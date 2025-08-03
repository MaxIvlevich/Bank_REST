package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.InvalidOperationException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.query.CardQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
class CardStatusManager {
    private final CardRepository cardRepository;
    private final CardQueryService cardQueryService;

    Card processStatusChange(UUID cardId, CardStatus newStatus, String actionName, CardStatus... expectedStatuses) {
        log.info("PROCESS_STATUS_CHANGE: [cardId={}, newStatus={}, action={}].", cardId, newStatus, actionName);

        Card card = cardQueryService.findByIdOrThrow(cardId);


        if (card.getStatus() == newStatus) {
            log.warn("PROCESS_STATUS_CHANGE_WARN: [cardId={}]. Card is already in target state {}.", cardId, newStatus);
            return card;
        }

        validateCurrentStatus(card, actionName, expectedStatuses);

        if (newStatus == CardStatus.ACTIVE) {
            ensureCardIsNotExpired(card);
        }

        card.setStatus(newStatus);
        Card savedCard = cardRepository.save(card);
        log.info("PROCESS_STATUS_CHANGE_SUCCESS: [cardId={}]. New status: {}.", cardId, newStatus);

        return savedCard;
    }

    private void validateCurrentStatus(Card card, String actionName, CardStatus... expectedStatuses) {
        if (expectedStatuses.length > 0) {
            boolean isStatusValid = Arrays.stream(expectedStatuses)
                    .anyMatch(expected -> expected == card.getStatus());
            if (!isStatusValid) {
                throw new InvalidOperationException(
                        String.format("Cannot %s. Card %s is in status %s, but expected one of %s.",
                                actionName, card.getId(), card.getStatus(), Arrays.toString(expectedStatuses))
                );
            }
        }
    }

    private void ensureCardIsNotExpired(Card card) {
        if (card.getExpirationDate().isBefore(YearMonth.now())) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
            log.error("PROCESS_STATUS_CHANGE_FAIL: [cardId={}]. Reason: Card has expired. Status updated to EXPIRED.", card.getId());
            throw new InvalidOperationException("Operation failed because the card is expired.");
        }
    }
}
