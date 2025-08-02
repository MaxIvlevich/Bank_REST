package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.request.TokenRefreshRequest;
import com.example.bankcards.dto.response.JwtResponse;
import com.example.bankcards.dto.response.TokenRefreshResponse;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.DuplicateResourceException;
import com.example.bankcards.exception.TokenRefreshException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.RefreshTokenService;
import com.example.bankcards.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final  UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto registerUser(RegistrationRequest request) {
        log.info("REGISTER_ATTEMPT: [username={}].", request.username());
        if (userRepository.existsByUsername(request.username())) {
            log.warn("REGISTER_FAIL: [username={}]. Reason: Username already taken.", request.username());
            throw new DuplicateResourceException("User", "username", request.username());

        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(Role.ROLE_USER));
        userRepository.save(user);
        log.info("REGISTER_SUCCESS: [userId={}, username={}].", user.getId(), user.getUsername());
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public JwtResponse loginUser(LoginRequest request) {
        log.info("LOGIN_ATTEMPT: [username={}].", request.identifier());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User userPrincipal = (User) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(authentication);
        String refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId()).getToken();
        Set<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        log.info("LOGIN_SUCCESS: [userId={}, username={}].", userPrincipal.getId(), userPrincipal.getUsername());
        return new JwtResponse(
                accessToken,
                refreshToken,
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                roles
        );
    }

    @Override
    public void logoutUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User userPrincipal) {
            log.info("LOGOUT_ATTEMPT: [userId={}, username={}].", userPrincipal.getId(), userPrincipal.getUsername());
            refreshTokenService.deleteByUserId(userPrincipal.getId());
            log.info("LOGOUT_SUCCESS: [userId={}]. Refresh token deleted.", userPrincipal.getId());
        }else {
            log.warn("LOGOUT_FAIL: Principal is not an instance of User. Type: {}",
                    principal != null ? principal.getClass().getName() : "null");
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        log.info("TOKEN_REFRESH_ATTEMPT: [refreshToken=...{}].", request.refreshToken().substring(request.refreshToken().length() - 4));
        String requestRefreshToken = request.refreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = jwtService.generateAccessTokenForUser(user);
                    log.info("TOKEN_REFRESH_SUCCESS: [userId={}]. New access token generated.", user.getId());
                    return new TokenRefreshResponse(newAccessToken, requestRefreshToken);
                })
                .orElseThrow(() ->{
                    log.warn("TOKEN_REFRESH_FAIL: Refresh token not found in database.");
                    return new TokenRefreshException("Refresh token is not in database!");
                });
    }
}
