package com.example.bankcards.entity.enums;

public enum CardStatus {
    /**
     * The card is active and can be used for transactions.
     */
    ACTIVE,

    /**
     * The card is blocked and cannot be used.
     */
    BLOCKED,

    /**
     * The card's validity period has ended.
     */
    EXPIRED
}
