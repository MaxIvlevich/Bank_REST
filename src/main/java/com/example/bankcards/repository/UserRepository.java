package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
/**
 * Repository interface for User entity.
 */
@Repository
public interface  UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their username and eagerly fetches their roles,
     * but ONLY if the user is active (isActive = true).
     * This method is the single source of truth for authentication.
     *
     * @param username The username to search for.
     * @return An Optional containing the found active user with roles initialized.
     */
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username AND u.isEnabled = true")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);;

    /**
     * Checks if a user with the given username exists.
     * This is an optimized way to check for uniqueness during registration.
     *
     * @param username the username to check.
     * @return true if a user with the username exists, false otherwise.
     */
    Boolean existsByUsername(String username);

    /**
     * Finds a user by ID and eagerly fetches their associated cards and profile.
     * This query uses LEFT JOIN FETCH to prevent N+1 select problems.
     *
     * @param id The ID of the user.
     * @return An Optional containing the user with their cards and profile initialized.
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.cards " +
            "LEFT JOIN FETCH u.userProfile " +
            "WHERE u.id = :id")
    Optional<User> findByIdWithDetails(@Param("id") UUID id);

    /**
     * Finds any user by their ID, ignoring the 'is_active' flag.
     * Intended for admin use.
     * @param id The ID of the user.
     * @return An Optional containing the user, regardless of their active status.
     */
    @Query("select u from User u where u.id = :id")
    Optional<User> findById_Admin(@Param("id") UUID id);

    /**
     * Finds all users, ignoring the 'is_active' flag.
     * Intended for admin use.
     * @param pageable Pagination information.
     * @return A page of all users.
     */
    @Query("select u from User u")
    Page<User> findAll_Admin(Pageable pageable);
}
