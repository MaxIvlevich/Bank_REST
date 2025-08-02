package com.example.bankcards.controller;

import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.UUID;

@Tag(name = "Card Management", description = "Endpoints for managing user bank cards")
@RequestMapping("/api/cards")
@SecurityRequirement(name = "bearerAuth")
public interface CardController {
    @Operation(summary = "Get my cards", description = "Returns a paginated list of my active cards.")
    @GetMapping("/my")
    ResponseEntity<Page<CardResponse>> getMyCards(@AuthenticationPrincipal User user, Pageable pageable);

    @Operation(summary = "Get my card by ID", description = "Returns details of a specific card if it belongs to me.")
    @GetMapping("/my/{cardId}")
    ResponseEntity<CardResponse> getMyCardById(@AuthenticationPrincipal User user,
                                               @Parameter(description = "ID of the card to be fetched") @PathVariable UUID cardId);

    @Operation(summary = "Get my card's balance", description = "Returns the current balance of my specific card.")
    @GetMapping("/my/{cardId}/balance")
    ResponseEntity<BigDecimal> getMyCardBalance(@AuthenticationPrincipal User user,
                                                @Parameter(description = "ID of the card") @PathVariable UUID cardId);

    @Operation(summary = "Request to block my card", description = "Submits a request to block one of my cards. The status will be changed to BLOCK_REQUESTED.")
    @PostMapping("/my/{cardId}/block-request")
    ResponseEntity<CardResponse> requestCardBlock(@AuthenticationPrincipal User user,
                                                  @Parameter(description = "ID of the card to block") @PathVariable UUID cardId);

    @Operation(summary = "Transfer between my cards", description = "Transfers a specified amount between two of my cards.")
    @PostMapping("/my/transfer")
    ResponseEntity<Void> transferBetweenMyCards(@AuthenticationPrincipal User user,
                                                @Valid @RequestBody TransferRequest request);
}
