package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.util.encryption.CardNumberEncryptor;
import com.example.bankcards.mapper.YearMonthDateConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The card number, encrypted in the database.
     * The CardNumberEncryptor class will handle encryption/decryption automatically.
     */
    @Convert(converter = CardNumberEncryptor.class)
    @Column(name = "card_number", nullable = false, unique = true)
    private String cardNumber;

    /**
     * The expiration date of the card (year and month).
     */
    @Column(name = "expiration_date", nullable = false)
    @Convert(converter = YearMonthDateConverter.class)
    private YearMonth expirationDate;

    /**
     * The current status of the card (e.g., ACTIVE, BLOCKED).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CardStatus status ;

    /**
     * The current balance of the card. BigDecimal is used for precision.
     */
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Flag for soft delete. If false, the card is considered deleted
     * and will not be fetched by standard repository queries due to @SQLRestriction.
     */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /**
     * The owner of the card. This creates a many-to-one relationship with the User entity.
     * LAZY fetch is used for performance optimization.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;
}
