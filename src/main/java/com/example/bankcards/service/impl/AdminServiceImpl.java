package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.UpdateProfileRequest;
import com.example.bankcards.dto.request.UpdateUserRolesRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.UserDetailResponse;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserProfile;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.DuplicateResourceException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.mapper.UserProfileMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AdminService;
import com.example.bankcards.service.query.CardQueryService;
import com.example.bankcards.service.query.UserQueryService;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.util.masking.CardMaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final UserMapper userMapper;
    private final CardStatusManager cardStatusManager;
    private final CardQueryService cardQueryService;
    private final UserQueryService userQueryService;
    private final UserProfileMapper userProfileMapper;

    @Override
    @Transactional
    public CardResponse createCard(CreateCardRequest request) {
        log.info("ADMIN_CREATE_CARD: [ownerId={}, cardNumber=...{}].",
                request.ownerId(), CardMaskingUtil.maskCardNumber(request.cardNumber()));
        if (cardRepository.existsByCardNumberAndActiveTrue(request.cardNumber())) {
            throw new DuplicateResourceException("Active Card", "cardNumber", request.cardNumber());
        }

        User owner = userQueryService.findByIdOrThrow(request.ownerId());
        BigDecimal balance = request.initialBalance() != null ? request.initialBalance() : BigDecimal.ZERO;

        Card newCard = new Card();
        newCard.setCardNumber(request.cardNumber());
        newCard.setExpirationDate(request.expirationDate());
        newCard.setOwner(owner);
        newCard.setStatus(CardStatus.ACTIVE);
        newCard.setBalance(balance.setScale(2, RoundingMode.HALF_UP));
        newCard.setActive(true);

        return cardMapper.toCardResponse(cardRepository.save(newCard));
    }

    @Override
    @Transactional
    public Page<CardResponse> findAllCards(Pageable pageable) {
        log.info("ADMIN_FIND_ALL_CARDS: [pageNumber={}, pageSize={}].",
                pageable.getPageNumber(), pageable.getPageSize());
        return cardRepository.findAll(pageable).map(cardMapper::toCardResponse);
    }

    @Override
    @Transactional
    public Page<CardResponse> findAllCardsByUserId(UUID userId, Pageable pageable) {
        log.info("ADMIN_FIND_CARDS_BY_USER: [userId={}, pageNumber={}, pageSize={}].",
                userId, pageable.getPageNumber(), pageable.getPageSize());
        return cardRepository.findAllByOwnerId(userId, pageable).map(cardMapper::toCardResponse);
    }

    @Override
    @Transactional
    public Page<CardResponse> findCardsByStatus(CardStatus status, Pageable pageable) {
        log.info("ADMIN_FIND_CARDS_BY_STATUS: [status={}, pageNumber={}, pageSize={}].",
                status, pageable.getPageNumber(), pageable.getPageSize());
        return cardRepository.findAllByStatus(status, pageable).map(cardMapper::toCardResponse);
    }

    @Override
    @Transactional
    public CardResponse activateCard(UUID cardId) {
        Card updatedCard = cardStatusManager.processStatusChange(cardId,
                CardStatus.ACTIVE, "activate card",
                CardStatus.BLOCKED, CardStatus.EXPIRED);
        return cardMapper.toCardResponse(updatedCard);
    }

    @Override
    @Transactional
    public CardResponse confirmCardBlock(UUID cardId) {
        Card updatedCard = cardStatusManager.processStatusChange(cardId, CardStatus.BLOCKED,
                "confirm block", CardStatus.BLOCK_REQUESTED);
        return cardMapper.toCardResponse(updatedCard);
    }

    @Override
    @Transactional
    public CardResponse declineCardBlock(UUID cardId) {
        Card updatedCard = cardStatusManager.processStatusChange(cardId,
                CardStatus.ACTIVE, "decline block", CardStatus.BLOCK_REQUESTED);
        return cardMapper.toCardResponse(updatedCard);
    }

    @Override
    @Transactional
    public void softDeleteCard(UUID cardId) {
        log.info("ADMIN_SOFT_DELETE_CARD: [cardId={}].", cardId);
        Card card = cardQueryService.findByIdOrThrow(cardId);
        card.setActive(false);
        cardRepository.save(card);
        log.info("ADMIN_SOFT_DELETE_CARD_SUCCESS: [cardId={}].", cardId);
    }

    @Override
    @Transactional
    public UserResponseDto findUserById(UUID userId) {
        log.info("ADMIN_FIND_USER_BY_ID: [userId={}].", userId);
        return userMapper.toUserResponseDto(userQueryService.findByIdOrThrow(userId));
    }

    @Override
    @Transactional
    public Page<UserResponseDto> findAllUsers(Pageable pageable) {
        log.info("ADMIN_FIND_ALL_USERS: [pageNumber={}, pageSize={}].",
                pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable).map(userMapper::toUserResponseDto);
    }

    @Override
    @Transactional
    public UserResponseDto updateUserRoles(UUID userId, UpdateUserRolesRequest request) {
        log.info("ADMIN_UPDATE_USER_ROLES: [userId={}, newRoles={}].",
                userId, request.roles());
        User user = userQueryService.findByIdOrThrow(userId);
        user.setRoles(request.roles());
        return userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto lockUserAccount(UUID userId) {
        log.info("ADMIN_LOCK_USER: [userId={}].", userId);
        User user = userQueryService.findByIdOrThrow(userId);
        user.setEnabled(false);
        return userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto unlockUserAccount(UUID userId) {
        log.info("ADMIN_UNLOCK_USER: [userId={}].", userId);
        User user = userQueryService.findByIdOrThrow(userId);
        user.setEnabled(true);
        return userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDetailResponse findUserWithDetailsById(UUID userId) {
        log.info("ADMIN_FIND_USER_DETAILS: [userId={}].", userId);

        User user = userRepository.findByIdWithDetails(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return userMapper.toUserDetailResponse(user);
    }

    @Override
    @Transactional
    public Page<UserDetailResponse> findAllUsersWithCards(Pageable pageable) {
        log.info("ADMIN_FIND_ALL_USERS_WITH_CARDS: [pageNumber={}, pageSize={}].",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<User> userPage = userRepository.findAll(pageable);
        List<User> users = userPage.getContent();

        if (users.isEmpty()) {
            return userPage.map(userMapper::toUserDetailResponse);
        }

        List<UUID> userIds = users.stream().map(User::getId).toList();
        List<Card> cards = cardRepository.findAllByOwnerIdIn(userIds);

        Map<UUID, List<Card>> cardsByOwnerId = cards.stream()
                .collect(Collectors.groupingBy(card -> card.getOwner().getId()));

        // Мапим каждого пользователя в DTO вручную, подставляя нужный список карт
        List<UserDetailResponse> dtos = users.stream().map(user -> {
            List<Card> userCards = cardsByOwnerId.getOrDefault(user.getId(), Collections.emptyList());
            // Нам нужен новый метод в маппере
            return userMapper.toUserDetailResponse(user, userCards);
        }).toList();

        return new PageImpl<>(dtos, pageable, userPage.getTotalElements());
    }

    @Override
    @Transactional
    public UserDetailResponse updateUserProfile(UUID userId, UpdateProfileRequest request) {
        log.info("ADMIN_UPDATE_USER_PROFILE: [userId={}].", userId);

        User user = userQueryService.findByIdOrThrow(userId);

        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            user.setUserProfile(profile);
        }
        userProfileMapper.updateProfileFromDto(request, profile);
                                                                                     
        User updatedUser = userRepository.save(user);
        return userMapper.toUserDetailResponse(updatedUser);
    }
}
