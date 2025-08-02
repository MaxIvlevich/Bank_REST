package com.example.bankcards.entity.enums;

public enum CardStatus {
    /**
     * The card is active and can be used for transactions.
     */
    ACTIVE,

    /** A user has requested to block the card, pending admin approval. */
    BLOCK_REQUESTED,

    /**
     * The card is blocked and cannot be used.
     */
    BLOCKED,

    /**
     * The card's validity period has ended.
     */
    EXPIRED
}
