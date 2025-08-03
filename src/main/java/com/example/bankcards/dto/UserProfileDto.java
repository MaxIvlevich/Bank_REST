package com.example.bankcards.dto;
/**
 * DTO for representing a user's profile information.
 */
public record  UserProfileDto (
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {
}
