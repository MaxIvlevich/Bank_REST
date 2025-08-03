package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.UpdateProfileRequest;
import com.example.bankcards.dto.request.UpdateUserRolesRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.PagedResponse;
import com.example.bankcards.dto.response.UserDetailResponse;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.entity.enums.CardStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@Tag(name = "Admin Panel", description = "Endpoints for administrative tasks")
@RequestMapping("/api/admin")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public interface AdminController {

    @Operation(summary = "Find all cards in the system", description = "Returns a paginated list of all cards, including inactive.")
    @GetMapping("/cards")
    ResponseEntity<PagedResponse<CardResponse>> getAllCards(Pageable pageable);

    @Operation(summary = "Find all cards for a specific user", description = "Returns a paginated list of all cards for a given user.")
    @GetMapping("/users/{userId}/cards")
    ResponseEntity<PagedResponse<CardResponse>> getAllCardsForUser(@Parameter(description = "ID of the user") @PathVariable UUID userId, Pageable pageable);

    @Operation(summary = "Find cards by status", description = "Returns a paginated list of all cards filtered by status.")
    @GetMapping("/cards/status/{status}")
    ResponseEntity<PagedResponse<CardResponse>> getCardsByStatus(@Parameter(description = "Card status") @PathVariable CardStatus status, Pageable pageable);

    @Operation(summary = "Create a new card for a user")
    @PostMapping("/cards")
    ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest request);

    @Operation(summary = "Activate a card")
    @PostMapping("/cards/{cardId}/activate")
    ResponseEntity<CardResponse> activateCard(@PathVariable UUID cardId);

    @Operation(summary = "Confirm a card block request")
    @PostMapping("/cards/{cardId}/block/confirm")
    ResponseEntity<CardResponse> confirmCardBlock(@PathVariable UUID cardId);

    @Operation(summary = "Decline a card block request")
    @PostMapping("/cards/{cardId}/block/decline")
    ResponseEntity<CardResponse> declineCardBlock(@PathVariable UUID cardId);

    @Operation(summary = "Soft-delete a card")
    @DeleteMapping("/cards/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    ResponseEntity<Void> softDeleteCard(@PathVariable UUID cardId);

    // --- User Management ---
    @Operation(summary = "Find all users")
    @GetMapping("/users")
    ResponseEntity<PagedResponse<UserResponseDto>> getAllUsers(Pageable pageable);

    @Operation(summary = "Find user by ID")
    @GetMapping("/users/{userId}")
    ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID userId);

    @Operation(summary = "Update user roles")
    @PutMapping("/users/{userId}/roles")
    ResponseEntity<UserResponseDto> updateUserRoles(@PathVariable UUID userId, @Valid @RequestBody UpdateUserRolesRequest request);

    @Operation(summary = "Lock a user account")
    @PostMapping("/users/{userId}/lock")
    ResponseEntity<UserResponseDto> lockUserAccount(@PathVariable UUID userId);

    @Operation(summary = "Unlock a user account")
    @PostMapping("/users/{userId}/unlock")
    ResponseEntity<UserResponseDto> unlockUserAccount(@PathVariable UUID userId);

    @Operation(summary = "Find user by ID with full details",
            description = "Returns detailed information about a user, including their profile and list of cards.")
    @GetMapping("/users/{userId}/details")
    ResponseEntity<UserDetailResponse> getUserDetailsById(@PathVariable UUID userId);

    @Operation(summary = "Find all users with their cards",
            description = "Returns a paginated list of all users, including their card lists.")
    @GetMapping("/users/with-cards")
    ResponseEntity<PagedResponse<UserDetailResponse>> getAllUsersWithCards(Pageable pageable);

    @Operation(summary = "Update user profile")
    @PutMapping("/users/{userId}/profile")
    ResponseEntity<UserDetailResponse> updateUserProfile(@PathVariable UUID userId, @Valid @RequestBody UpdateProfileRequest request);
}
