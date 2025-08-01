package com.example.bankcards.security.impl;

import com.example.bankcards.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils implements JwtService {
    @Value("${app.jwt.secret}")
    private String jwtSecretString;
    @Value("${app.jwt.expirationMs}")
    private Long accessTokenDurationMs;

    private SecretKey key;


    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretString);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateAccessToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenDurationMs))
                .signWith(key)
                .compact();
    }

    @Override
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException expiredJwtException) {
            log.error("ExpiredJwtException", expiredJwtException);
        } catch (UnsupportedJwtException exception) {
            log.error("UnsupportedJwtException", exception);
        } catch (MalformedJwtException exception) {
            log.error("MalformedJwtException", exception);
        } catch (SignatureException exception) {
            log.error("SignatureException ", exception);
        } catch (Exception exception) {
            log.error("Invalid token", exception);
        }
        return false;
    }
}
