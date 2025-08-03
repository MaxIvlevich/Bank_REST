package com.example.bankcards.service;

import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidOperationException;
import com.example.bankcards.exception.UnauthorizedOperationException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import com.example.bankcards.service.query.CardQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardServiceImplTest {
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardQueryService cardQueryService;
    // CardMapper не нужен для теста transferBetweenMyCards, так как метод ничего не возвращает

    @InjectMocks
    private CardServiceImpl cardService;

    private Card fromCard;
    private Card toCard;
    private UUID userId;

    @BeforeEach
        // Этот метод будет выполняться перед каждым тестом
    void setUp() {
        userId = UUID.randomUUID();
        // Тестовые данные, которые будем переиспользовать
        User testUser = new User();
        testUser.setId(userId);

        fromCard = new Card();
        fromCard.setId(UUID.randomUUID());
        fromCard.setOwner(testUser);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(new BigDecimal("1000.00"));

        toCard = new Card();
        toCard.setId(UUID.randomUUID());
        toCard.setOwner(testUser);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Transfer should succeed when all conditions are met")
    void transferBetweenMyCards_shouldSucceed_whenAllConditionsMet() {
        // Arrange
        TransferRequest request = new TransferRequest(fromCard.getId(), toCard.getId(), new BigDecimal("100.00"));

        // Настраиваем моки
        when(cardQueryService.findActiveByIdWithLockOrThrow(fromCard.getId())).thenReturn(fromCard);
        when(cardQueryService.findActiveByIdWithLockOrThrow(toCard.getId())).thenReturn(toCard);

        // Act
        cardService.transferBetweenMyCards(request, userId);

        // Assert
        assertEquals(new BigDecimal("900.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("600.00"), toCard.getBalance());

        verify(cardRepository, times(1)).saveAll(anyList()); // Проверяем, что обе карты были сохранены
    }

    @Test
    @DisplayName("Transfer should throw InsufficientFundsException when balance is too low")
    void transferBetweenMyCards_shouldThrowInsufficientFundsException_whenBalanceIsTooLow() {
        // Arrange
        // Сумма перевода больше, чем баланс
        TransferRequest request = new TransferRequest(fromCard.getId(), toCard.getId(), new BigDecimal("2000.00"));

        when(cardQueryService.findActiveByIdWithLockOrThrow(fromCard.getId())).thenReturn(fromCard);
        when(cardQueryService.findActiveByIdWithLockOrThrow(toCard.getId())).thenReturn(toCard);

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> {
            cardService.transferBetweenMyCards(request, userId);
        });

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Transfer should throw InvalidOperationException when source card is not active")
    void transferBetweenMyCards_shouldThrowInvalidOperationException_whenSourceCardIsNotActive() {
        // Arrange
        fromCard.setStatus(CardStatus.BLOCKED); // Блокируем карту
        TransferRequest request = new TransferRequest(fromCard.getId(), toCard.getId(), new BigDecimal("100.00"));

        when(cardQueryService.findActiveByIdWithLockOrThrow(fromCard.getId())).thenReturn(fromCard);
        when(cardQueryService.findActiveByIdWithLockOrThrow(toCard.getId())).thenReturn(toCard);

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> {
            cardService.transferBetweenMyCards(request, userId);
        });
    }

    @Test
    @DisplayName("Transfer should throw UnauthorizedOperationException when a card is not owned by the user")
    void transferBetweenMyCards_shouldThrowUnauthorizedOperationException_whenCardNotOwned() {

        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        toCard.setOwner(anotherUser);

        TransferRequest request = new TransferRequest(fromCard.getId(), toCard.getId(), new BigDecimal("100.00"));

        when(cardQueryService.findActiveByIdWithLockOrThrow(fromCard.getId())).thenReturn(fromCard);
        when(cardQueryService.findActiveByIdWithLockOrThrow(toCard.getId())).thenReturn(toCard);

        // Act & Assert
        assertThrows(UnauthorizedOperationException.class, () -> {
            cardService.transferBetweenMyCards(request, userId);
        });
    }

}
