package com.example.bankcards.dto.response;

import com.example.bankcards.dto.UserProfileDto;
import com.example.bankcards.entity.enums.Role;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A detailed DTO for a user, including their profile and a list of cards.
 * Intended for admin use.
 */
public record UserDetailResponse(
        UUID id,
        String username,
        Set<Role> roles,
        boolean isActive,
        UserProfileDto profile,
        List<CardResponse> cards
) {

}
