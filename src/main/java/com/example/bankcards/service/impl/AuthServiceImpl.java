package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.request.TokenRefreshRequest;
import com.example.bankcards.dto.response.TokenRefreshResponse;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.dto.response.JwtResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.RefreshTokenService;
import com.example.bankcards.util.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.GrantedAuthority;
import com.example.bankcards.entity.RefreshToken;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        if (userRepository.existsByUsername(request.username())) {
            // TODO Заменить на кастомное исключение
            throw new RuntimeException("Error: Username is already taken!");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(Role.ROLE_USER));
        userRepository.save(user);
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public JwtResponse loginUser(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User userPrincipal = (User) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(authentication);
        String refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId()).getToken();
        Set<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

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
            refreshTokenService.deleteByUserId(userPrincipal.getId());
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.refreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = jwtService.generateAccessTokenForUser(user);
                    return new TokenRefreshResponse(newAccessToken, requestRefreshToken);
                })
                .orElseThrow(() -> new RuntimeException( // TODO: Заменить на кастомное исключение TokenRefreshException
                        "Refresh token is not in database or is invalid!"));
    }
}
