package com.example.bankcards.service.impl;

import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.InvalidOperationException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.AdminService;
import com.example.bankcards.service.impl.CardStatusManager;
import com.example.bankcards.service.query.CardQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardStatusManagerTest {
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardQueryService cardQueryService;
    @InjectMocks
    private CardStatusManager cardStatusManager;

    private Card testCard;
    private UUID cardId;

    @BeforeEach
    void setUp() {
        cardId = UUID.randomUUID();
        testCard = new Card();
        testCard.setId(cardId);
        testCard.setExpirationDate(YearMonth.now().plusYears(1));
    }

    @Test
    @DisplayName("processStatusChange should activate a blocked card successfully")
    void processStatusChange_shouldActivateBlockedCard() {
        // Arrange
        testCard.setStatus(CardStatus.BLOCKED);
        when(cardQueryService.findByIdOrThrow(cardId)).thenReturn(testCard);
        when(cardRepository.save(testCard)).thenReturn(testCard);

        // Act
        Card result = cardStatusManager.processStatusChange(cardId, CardStatus.ACTIVE, "activate", CardStatus.BLOCKED);

        // Assert
        assertNotNull(result);
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        verify(cardRepository, times(1)).save(testCard);
    }

    @Test
    @DisplayName("processStatusChange should throw InvalidOperationException for wrong expected status")
    void processStatusChange_shouldThrowException_forWrongExpectedStatus() {
        // Arrange
        testCard.setStatus(CardStatus.ACTIVE); // Карта активна
        when(cardQueryService.findByIdOrThrow(cardId)).thenReturn(testCard);

        // Act & Assert
        // А мы пытаемся подтвердить блокировку, которая требует статуса BLOCK_REQUESTED
        assertThrows(InvalidOperationException.class, () -> {
            cardStatusManager.processStatusChange(cardId, CardStatus.BLOCKED, "confirm block", CardStatus.BLOCK_REQUESTED);
        });

        // Проверяем, что сохранение не вызывалось
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("processStatusChange should throw InvalidOperationException when activating an expired card")
    void processStatusChange_shouldThrowException_whenActivatingExpiredCard() {
        // Arrange
        testCard.setStatus(CardStatus.BLOCKED);
        testCard.setExpirationDate(YearMonth.now().minusMonths(1)); // Карта просрочена
        when(cardQueryService.findByIdOrThrow(cardId)).thenReturn(testCard);

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> {
            cardStatusManager.processStatusChange(cardId, CardStatus.ACTIVE, "activate", CardStatus.BLOCKED);
        });

        // Проверяем, что было вызвано сохранение для установки статуса EXPIRED
        assertEquals(CardStatus.EXPIRED, testCard.getStatus());
        verify(cardRepository, times(1)).save(testCard);
    }

}
