package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.UpdateUserRolesRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.DuplicateResourceException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.mapper.UserProfileMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.query.CardQueryService;
import com.example.bankcards.service.query.UserQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the AdminServiceImpl class.
 */
@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private UserQueryService userQueryService;
    @Mock
    private CardQueryService cardQueryService;
    @Mock
    private UserProfileMapper userProfileMapper;

    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        // Создаем РЕАЛЬНЫЙ CardStatusManager, но с МОК-зависимостями
        CardStatusManager cardStatusManager = new CardStatusManager(cardRepository, cardQueryService);

        // Создаем РЕАЛЬНЫЙ AdminService, передавая ему РЕАЛЬНЫЙ CardStatusManager
        adminService = new AdminServiceImpl(
                cardRepository,
                userRepository,
                cardMapper,
                userMapper,
                cardStatusManager,
                cardQueryService,
                userQueryService,
                userProfileMapper
        );
    }

    @Test
    @DisplayName("lockUserAccount should set user to inactive and save")
    void lockUserAccount_shouldSetUserToInactiveAndSave() {

        UUID userId = UUID.randomUUID();
        User userToLock = new User();
        userToLock.setId(userId);
        userToLock.setUsername("testuser");
        userToLock.setEnabled(true);
        UserResponseDto expectedDto = new UserResponseDto(userId, "testuser", null, false);

        when(userQueryService.findByIdOrThrow(any(UUID.class))).thenReturn(userToLock);
        when(userRepository.save(any(User.class))).thenReturn(userToLock);
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(expectedDto);


        UserResponseDto actualDto = adminService.lockUserAccount(userId);

        assertNotNull(actualDto);
        assertEquals(expectedDto, actualDto);
        assertFalse(userToLock.isEnabled());

        verify(userQueryService, times(1)).findByIdOrThrow(userId);
        verify(userRepository, times(1)).save(userToLock);
        verify(userMapper, times(1)).toUserResponseDto(userToLock);
    }

    @Test
    @DisplayName("findUserById should throw ResourceNotFoundException when user does not exist")
    void findUserById_shouldThrowException_whenUserNotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        // Обучаем мок выбрасывать исключение, когда его вызывают
        when(userQueryService.findByIdOrThrow(userId)).thenThrow(new ResourceNotFoundException("User", "id", userId));

        // Act & Assert
        // Проверяем, что вызов метода приводит к выбросу именно нашего исключения
        assertThrows(ResourceNotFoundException.class, () -> adminService.findUserById(userId));

        // Проверяем, что метод был вызван
        verify(userQueryService, times(1)).findByIdOrThrow(userId);
    }

    // --- Тесты для Card Management ---

    @Test
    @DisplayName("createCard should successfully create and return a card")
    void createCard_shouldCreateAndReturnCard_whenDataIsValid() {
        // Arrange
        UUID ownerId = UUID.randomUUID();
        CreateCardRequest request = new CreateCardRequest(ownerId, "1111222233334444", YearMonth.now().plusYears(1),null);

        User owner = new User();
        owner.setId(ownerId);

        // Настраиваем моки
        when(userQueryService.findByIdOrThrow(ownerId)).thenReturn(owner);
        when(cardRepository.existsByCardNumberAndActiveTrue(request.cardNumber())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardMapper.toCardResponse(any(Card.class))).thenReturn(mock(CardResponse.class));

        // Act
        CardResponse result = adminService.createCard(request);

        // Assert
        assertNotNull(result);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    @DisplayName("createCard should throw DuplicateResourceException when card number exists")
    void createCard_shouldThrowException_whenCardNumberExists() {
        // Arrange
        CreateCardRequest request = new CreateCardRequest(UUID.randomUUID(), "1111222233334444", YearMonth.now().plusYears(1),null);
        when(cardRepository.existsByCardNumberAndActiveTrue(request.cardNumber())).thenReturn(true);


        assertThrows(DuplicateResourceException.class, () -> adminService.createCard(request));

        verify(userQueryService, never()).findByIdOrThrow(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("softDeleteCard should set card to inactive")
    void softDeleteCard_shouldSetCardToInactive() {
        // Arrange
        UUID cardId = UUID.randomUUID();
        Card card = new Card();
        card.setId(cardId);
        card.setActive(true);

        when(cardQueryService.findByIdOrThrow(cardId)).thenReturn(card);

        // Act
        adminService.softDeleteCard(cardId);

        // Assert
        assertFalse(card.isActive());
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    @DisplayName("unlockUserAccount should set user to active and save")
    void unlockUserAccount_shouldSetUserToActiveAndSave() {
        // --- Arrange ---
        UUID userId = UUID.randomUUID();
        User userToUnlock = new User();
        userToUnlock.setId(userId);
        userToUnlock.setEnabled(false); // Изначально пользователь заблокирован

        UserResponseDto expectedDto = new UserResponseDto(userId, "testuser", null, true);

        // Настраиваем моки
        when(userQueryService.findByIdOrThrow(userId)).thenReturn(userToUnlock);
        when(userRepository.save(userToUnlock)).thenReturn(userToUnlock);
        when(userMapper.toUserResponseDto(userToUnlock)).thenReturn(expectedDto);

        // --- Act ---
        UserResponseDto actualDto = adminService.unlockUserAccount(userId);

        // --- Assert ---
        assertNotNull(actualDto);
        assertEquals(expectedDto, actualDto);
        assertTrue(userToUnlock.isEnabled());

        verify(userQueryService, times(1)).findByIdOrThrow(userId);
        verify(userRepository, times(1)).save(userToUnlock);
    }

    @Test
    @DisplayName("updateUserRoles should change roles and save user")
    void updateUserRoles_shouldChangeRolesAndSave() {
        // --- Arrange ---
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setRoles(Set.of(Role.ROLE_USER));

        // Запрос на обновление ролей
        Set<Role> newRoles = Set.of(Role.ROLE_ADMIN, Role.ROLE_USER);
        UpdateUserRolesRequest request = new UpdateUserRolesRequest(newRoles);

        UserResponseDto expectedDto = new UserResponseDto(userId, "testuser", newRoles, true);

        // Настраиваем моки
        when(userQueryService.findByIdOrThrow(userId)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDto(user)).thenReturn(expectedDto);

        // --- Act ---
        adminService.updateUserRoles(userId, request);

        // --- Assert ---
        assertEquals(newRoles, user.getRoles());
        verify(userQueryService, times(1)).findByIdOrThrow(userId);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toUserResponseDto(user);
    }

    @Test
    @DisplayName("activateCard should do nothing and not fail if card is already active")
    void activateCard_shouldDoNothing_ifCardIsAlreadyActive() {
        // --- Arrange ---
        UUID cardId = UUID.randomUUID();
        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(YearMonth.now().plusYears(1));

        when(cardQueryService.findByIdOrThrow(cardId)).thenReturn(card);
        when(cardMapper.toCardResponse(any(Card.class))).thenReturn(mock(CardResponse.class));

        // --- Act ---
        assertDoesNotThrow(() -> {
            adminService.activateCard(cardId);
        });

        // --- Assert ---
        verify(cardRepository, never()).save(any(Card.class));
        // Проверим, что маппер  был вызван
        verify(cardMapper, times(1)).toCardResponse(card);
    }
}
