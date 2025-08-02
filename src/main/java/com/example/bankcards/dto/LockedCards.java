package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;

public record LockedCards(
        Card fromCard,
        Card toCard
) {
}
