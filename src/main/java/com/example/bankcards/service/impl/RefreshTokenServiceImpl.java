package com.example.bankcards.service.impl;

import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${app.jwt.refreshExpirationMs}")
    private Long refreshTokenDurationMs;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        deleteByUserId(userId);
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("Error: User not found with id " + userId) // TODO Replace with custom exception
        ));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");// TODO Replace with custom exception
        }
        return token;
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        refreshTokenRepository.deleteByUserid(userId);

    }
}
