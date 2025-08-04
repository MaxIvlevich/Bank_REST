package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface CardRepository  extends JpaRepository<Card, UUID> {

    // =========== USER-FACING METHODS (only active cards) ===========

    Optional<Card> findByIdAndOwnerIdAndActiveTrue(UUID id, UUID ownerId);

    Page<Card> findAllByOwnerIdAndActiveTrue(UUID ownerId, Pageable pageable);

    boolean existsByCardNumberAndActiveTrue(String cardNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Card c where c.id = :id and c.active = true")
    Optional<Card> findActiveByIdWithLock(@Param("id") UUID id);


    // =========== ADMIN-FACING METHODS  ===========

    Page<Card> findAllByOwnerId(UUID ownerId, Pageable pageable);

    Page<Card> findAllByStatus(CardStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Card c where c.id = :id")
    Optional<Card> findByIdWithLock(@Param("id") UUID id);

    /**
     * Finds all cards owned by a list of users.
     * @param ownerIds A list of user IDs.
     * @return A list of cards.
     */
    List<Card> findAllByOwnerIdIn(List<UUID> ownerIds);


    boolean existsByCardNumberHash(String cardNumberHash);
}
