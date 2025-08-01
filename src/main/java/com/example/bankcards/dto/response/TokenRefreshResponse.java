package com.example.bankcards.dto.response;

public record TokenRefreshResponse (
        String accessToken,
        String refreshToken
){
}
