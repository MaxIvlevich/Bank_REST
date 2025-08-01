package com.example.bankcards.dto.response;

import java.util.List;
import java.util.UUID;

public record JwtResponse (
        String accessToken,
        String refreshToken,
        UUID id,
        String username,
        String email,
        List<String> roles
){
}
