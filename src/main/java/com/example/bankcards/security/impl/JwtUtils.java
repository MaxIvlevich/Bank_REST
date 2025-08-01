package com.example.bankcards.security.impl;

import com.example.bankcards.entity.User;
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
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
        return buildToken(userPrincipal.getUsername(), getRoles(userPrincipal.getAuthorities()));
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

    @Override
    public String generateAccessTokenForUser(User user) {
        return buildToken(user.getUsername(), getRoles(user.getAuthorities()));
    }

    /**
     * Extracts a list of role names from a collection of GrantedAuthority objects.
     *
     * @param authorities The collection of authorities.
     * @return A list of role names as strings.
     */
    private List<String> getRoles(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
    /**
     * Private helper method to build the JWT with common claims.
     *
     * @param username The subject of the token.
     * @param roles    The list of roles to include as a claim.
     * @return A string representation of the JWT.
     */
    private String buildToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenDurationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("roles", roles)
                .signWith(key)
                .compact();
    }
}
