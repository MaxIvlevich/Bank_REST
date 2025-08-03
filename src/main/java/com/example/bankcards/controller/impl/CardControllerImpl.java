package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.CardController;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.PagedResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CardControllerImpl implements CardController{
    private final CardService cardService;

    @Override
    public ResponseEntity<PagedResponse<CardResponse>> getMyCards(@AuthenticationPrincipal User user, Pageable pageable) {
        Page<CardResponse> cards = cardService.findMyCards(user.getId(), pageable);
        return ResponseEntity.ok(PagedResponse.from(cards));
    }

    @Override
    public ResponseEntity<CardResponse> getMyCardById(@AuthenticationPrincipal User user, UUID cardId) {
        CardResponse card = cardService.findMyCardById(cardId, user.getId());
        return ResponseEntity.ok(card);
    }

    @Override
    public ResponseEntity<BigDecimal> getMyCardBalance(@AuthenticationPrincipal User user, UUID cardId) {
        BigDecimal balance = cardService.getMyCardBalance(cardId, user.getId());
        return ResponseEntity.ok(balance);
    }

    @Override
    public ResponseEntity<CardResponse> requestCardBlock(@AuthenticationPrincipal User user, UUID cardId) {
        CardResponse card = cardService.requestCardBlock(cardId, user.getId());
        return ResponseEntity.ok(card);
    }

    @Override
    public ResponseEntity<Void> transferBetweenMyCards(@AuthenticationPrincipal User user, TransferRequest request) {
        cardService.transferBetweenMyCards(request, user.getId());
        return ResponseEntity.ok().build();
    }
}
