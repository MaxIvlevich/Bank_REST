package com.example.bankcards.controller;

import com.example.bankcards.AbstractIntegrationTest;
import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class AuthControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Очищаем базу данных после каждого теста, чтобы тесты не влияли друг на друга.
     */
    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        cardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/register - Success")
    void registerUser_whenUsernameIsUnique_shouldReturn200AndUserData() throws Exception {
        // --- Arrange ---
        RegistrationRequest request = new RegistrationRequest("testuser", "password123");

        // --- Act & Assert ---
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                // Убираем явное приведение типов (ResultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_USER")))
                .andExpect(jsonPath("$.isActive").value(true));

        // Проверяем состояние базы данных
        Optional<User> savedUserOpt = userRepository.findByUsernameWithRoles("testuser"); // Используем обычный findByUsername
        assertTrue(savedUserOpt.isPresent(), "User should be saved in the database");

        User savedUser = savedUserOpt.get();
        // Проверяем, что пароль в базе захеширован
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
    }

    @Test
    @DisplayName("POST /api/auth/register - Conflict")
    void registerUser_whenUsernameExists_shouldReturn409Conflict() throws Exception {
        // --- Arrange ---
        RegistrationRequest initialRequest = new RegistrationRequest("testuser", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialRequest)))
                .andExpect(status().isOk());

        RegistrationRequest duplicateRequest = new RegistrationRequest("testuser", "anotherPassword");

        // --- Act & Assert ---
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                // Убираем явное приведение типов (ResultMatcher)
                .andExpect(jsonPath("$.message").value("User with username 'testuser' already exists."));
    }

    @Test
    @DisplayName("POST /api/auth/login - Success")
    void loginUser_withValidCredentials_shouldReturn200AndTokens() throws Exception {
        // --- Arrange ---
        // Сначала нужно зарегистрировать пользователя, чтобы было кем логиниться
        RegistrationRequest registrationRequest = new RegistrationRequest("loginuser", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk());

        // Готовим запрос на логин
        LoginRequest loginRequest = new LoginRequest("loginuser", "password123");

        // --- Act & Assert ---
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("loginuser"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/login - Failure")
    void loginUser_withInvalidCredentials_shouldReturn401Unauthorized() throws Exception {
        // --- Arrange ---
        // Регистрируем пользователя
        RegistrationRequest registrationRequest = new RegistrationRequest("loginuser", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk());

        // Готовим запрос на логин с НЕВЕРНЫМ паролем
        LoginRequest loginRequest = new LoginRequest("loginuser", "wrongpassword");

        // --- Act & Assert ---
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                // Spring Security по умолчанию возвращает 401 для BadCredentialsException
                .andExpect(status().isUnauthorized());
    }
}
