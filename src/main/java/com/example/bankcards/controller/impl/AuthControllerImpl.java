package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.AuthController;
import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.request.TokenRefreshRequest;
import com.example.bankcards.dto.response.JwtResponse;
import com.example.bankcards.dto.response.TokenRefreshResponse;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {
    private final AuthService authService;
    @Override
    public ResponseEntity<UserResponseDto> registerUser(RegistrationRequest registrationRequest) {
        UserResponseDto registeredUser = authService.registerUser(registrationRequest);
        return ResponseEntity.ok(registeredUser);
    }

    @Override
    public ResponseEntity<JwtResponse> loginUser(LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.loginUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @Override
    public ResponseEntity<String> logoutUser() {
        authService.logoutUser();
        return ResponseEntity.ok("User successfully logged out.");
    }

    @Override
    public ResponseEntity<TokenRefreshResponse> refreshToken(TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}
