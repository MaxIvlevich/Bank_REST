package com.example.bankcards.dto.auth;

public record TokenRefreshResponse (
        String accessToken,
        String refreshToken
){
}
