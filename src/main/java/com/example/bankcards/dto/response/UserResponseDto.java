package com.example.bankcards.dto.response;

import java.util.UUID;

public record UserResponseDto(
        UUID userId,
        String setUsername,
        String role
){
}
