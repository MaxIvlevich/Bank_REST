package com.example.bankcards.dto.response;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record JwtResponse (
        String accessToken,
        String refreshToken,
        UUID id,
        String username,
        Set<String> roles
){
}
