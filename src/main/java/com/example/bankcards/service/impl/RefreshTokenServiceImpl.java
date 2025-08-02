package com.example.bankcards.service.impl;

import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.TokenRefreshException;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
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
        log.info("CREATE_REFRESH_TOKEN: [userId={}].", userId);
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("Error: User not found with id " + userId)
        ));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("CREATE_REFRESH_TOKEN_SUCCESS: [userId={}, tokenId={}].", userId, savedToken.getId());
        return savedToken;
    }

    @Override
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        log.info("VERIFY_REFRESH_TOKEN_EXPIRATION: [tokenId={}].", token.getId());
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            log.warn("VERIFY_REFRESH_TOKEN_FAIL: [tokenId={}]. Reason: Token expired. Token deleted.", token.getId());
            throw new TokenRefreshException(token.getToken(), "Token has expired!");
        }
        return token;
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        log.info("DELETE_REFRESH_TOKEN: [userId={}].", userId);
        int deletedCount = refreshTokenRepository.deleteByUserId(userId); // Исправлено имя метода
        if (deletedCount > 0) {
            log.info("DELETE_REFRESH_TOKEN_SUCCESS: [userId={}]. Deleted {} token(s).", userId, deletedCount);
        } else {
            log.info("DELETE_REFRESH_TOKEN_NO_OP: [userId={}]. No active refresh token found to delete.", userId);
        }

    }
}
