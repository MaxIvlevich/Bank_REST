package com.example.bankcards.dto.response;

import com.example.bankcards.entity.enums.Role;

import java.util.Set;
import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String username,
        Set<Role> roles
){
}
