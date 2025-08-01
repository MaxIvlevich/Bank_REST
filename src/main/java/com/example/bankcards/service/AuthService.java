package com.example.bankcards.service;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.dto.response.JwtResponse;

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
}
