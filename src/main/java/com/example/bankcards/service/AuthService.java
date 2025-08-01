package com.example.bankcards.service;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.request.TokenRefreshRequest;
import com.example.bankcards.dto.response.JwtResponse;
import com.example.bankcards.dto.response.TokenRefreshResponse;
import com.example.bankcards.dto.response.UserResponseDto;

public interface AuthService {
    /**
     * Registers a new user in the system.
     *
     * @param request DTO containing user registration data (username, password).
     * @return A response DTO containing JWT tokens and user information.
     */
    UserResponseDto registerUser(RegistrationRequest request);

    /**
     * Authenticates an existing user and provides JWT tokens.
     *
     * @param request DTO containing user login credentials.
     * @return A response DTO containing JWT tokens and user information.
     */
    JwtResponse loginUser(LoginRequest request);

    /**
     * Logs out the user by invalidating their refresh token.
     */
    void logoutUser();

    /**
     * Refreshes an expired access token using a valid refresh token.
     *
     * @param request DTO containing the refresh token.
     * @return A response DTO containing a new access token and the original refresh token.
     */
    TokenRefreshResponse refreshToken(TokenRefreshRequest request);
}
