package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.AdminController;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.UpdateUserRolesRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AdminControllerImpl implements AdminController {

    private final AdminService adminService;
    @Override
    public ResponseEntity<Page<CardResponse>> getAllCards(Pageable pageable) {
        return ResponseEntity.ok(adminService.findAllCards(pageable));
    }

    @Override
    public ResponseEntity<Page<CardResponse>> getAllCardsForUser(UUID userId, Pageable pageable) {
        return ResponseEntity.ok(adminService.findAllCardsByUserId(userId, pageable));
    }

    @Override
    public ResponseEntity<Page<CardResponse>> getCardsByStatus(CardStatus status, Pageable pageable) {
        return ResponseEntity.ok(adminService.findCardsByStatus(status, pageable));
    }

    @Override
    public ResponseEntity<CardResponse> createCard(CreateCardRequest request) {
        return new ResponseEntity<>(adminService.createCard(request), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CardResponse> activateCard(UUID cardId) {
        return ResponseEntity.ok(adminService.activateCard(cardId));
    }

    @Override
    public ResponseEntity<CardResponse> confirmCardBlock(UUID cardId) {
        return ResponseEntity.ok(adminService.confirmCardBlock(cardId));
    }

    @Override
    public ResponseEntity<CardResponse> declineCardBlock(UUID cardId) {
        return ResponseEntity.ok(adminService.declineCardBlock(cardId));
    }

    @Override
    public ResponseEntity<Void> softDeleteCard(UUID cardId) {
        adminService.softDeleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(adminService.findAllUsers(pageable));
    }

    @Override
    public ResponseEntity<UserResponseDto> getUserById(UUID userId) {
        return ResponseEntity.ok(adminService.findUserById(userId));
    }

    @Override
    public ResponseEntity<UserResponseDto> updateUserRoles(UUID userId, UpdateUserRolesRequest request) {
        return ResponseEntity.ok(adminService.updateUserRoles(userId, request));
    }

    @Override
    public ResponseEntity<UserResponseDto> lockUserAccount(UUID userId) {
        return ResponseEntity.ok(adminService.lockUserAccount(userId));
    }

    @Override
    public ResponseEntity<UserResponseDto> unlockUserAccount(UUID userId) {
        return ResponseEntity.ok(adminService.unlockUserAccount(userId));
    }
}
