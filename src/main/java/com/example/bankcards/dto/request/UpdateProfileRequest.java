package com.example.bankcards.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating a user's profile information.
 * All fields are optional, allowing for partial updates.
 */
public record  UpdateProfileRequest (

        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @Email(message = "Email should be valid")
        String email,

        String phoneNumbe

){
}
