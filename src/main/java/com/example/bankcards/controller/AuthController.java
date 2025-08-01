package com.example.bankcards.controller;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.request.TokenRefreshRequest;
import com.example.bankcards.dto.response.JwtResponse;
import com.example.bankcards.dto.response.TokenRefreshResponse;
import com.example.bankcards.dto.response.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Authentication", description = "Endpoints for user registration and login")
@RequestMapping("/api/auth")
public interface AuthController {
    @Operation(summary = "Register a new user",
            description = "Creates a new user account. Returns the created user's information.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User successfully registered",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data or username already exists")
            })
    @PostMapping("/register")
    ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest);

    @Operation(summary = "Authenticate a user",
            description = "Logs in a user and returns JWT access and refresh tokens.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User successfully authenticated",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JwtResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            })
    @PostMapping("/login")
    ResponseEntity<JwtResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest);

    @Operation(
            summary = "Log out current user",
            description = "Performs a logout for the currently authenticated user. This invalidates the user's refresh token, " +
                    "preventing them from obtaining new access tokens. The current access token will remain valid until it expires.",
            security = { @SecurityRequirement(name = "bearerAuth") },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully logged out"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized if the user is not authenticated (no valid access token provided)"
                    )
            }
    )
    @PostMapping("/logout")
    ResponseEntity<String> logoutUser();

    @Operation(
            summary = "Refresh access token",
            description = "Obtains a new access token using a valid refresh token. " +
                    "This endpoint should be called when the access token has expired.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Tokens refreshed successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TokenRefreshResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request (e.g., blank refresh token)"
                    ),
                    @ApiResponse(
                            responseCode = "403", // Или 401, зависит от логики обработки исключений
                            description = "Refresh token is invalid or expired"
                    )
            }
    )
    @PostMapping("/refresh")
    ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request);
}
