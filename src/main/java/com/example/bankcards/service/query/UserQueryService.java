package com.example.bankcards.service.query;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * A specialized service for querying User entities.
 * Encapsulates common find-or-throw logic.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {
    private final UserRepository userRepository;

    /**
     * Finds a user by their ID or throws a ResourceNotFoundException.
     * This method is intended for admin use as it does not check the 'active' flag.
     *
     * @param userId The ID of the user to find.
     * @return The found User entity.
     */
    public User findByIdOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

}
