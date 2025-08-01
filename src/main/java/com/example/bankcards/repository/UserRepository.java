package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
/**
 * Repository interface for User entity.
 */
@Repository
public interface  UserRepository extends JpaRepository<User, UUID> {
    /**
     * Finds a user by their username.
     * This method is crucial for Spring Security's UserDetailsService.
     *
     * @param username the username to search for.
     * @return an Optional containing the found user or an empty Optional if not found.
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks if a user with the given username exists.
     * This is an optimized way to check for uniqueness during registration.
     *
     * @param username the username to check.
     * @return true if a user with the username exists, false otherwise.
     */
    Boolean existsByUsername(String username);
}
