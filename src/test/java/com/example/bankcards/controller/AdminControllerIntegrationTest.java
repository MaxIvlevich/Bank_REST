package com.example.bankcards.controller;

import com.example.bankcards.AbstractIntegrationTest;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.response.JwtResponse;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CardRepository cardRepository;
    private String adminToken;
    private String userToken;
    private UserResponseDto testUser;

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Очистка
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "refresh_tokens", "user_profiles", "user_roles", "cards", "users");

        // 2. Создаем админа в базе
        User admin = new User();
        admin.setUsername("test-admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRoles(new HashSet<>(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER)));
        admin.setEnabled(true);
        userRepository.save(admin);

        // 3. Создаем юзера  в базе
        user = new User();
        user.setUsername("test-user");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRoles(new HashSet<>(Set.of(Role.ROLE_USER)));
        user.setEnabled(true);
        testUser = userMapper.toUserResponseDto(userRepository.save(user));

        // 4. Получаем токены для них через API
        adminToken = loginAndGetToken("test-admin", "password");
        userToken = loginAndGetToken("test-user", "password");
    }

    @Test
    @DisplayName("GET /api/admin/users - Admin should get list of users")
    void getAllUsers_asAdmin_shouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2))); // admin + user
    }

    @Test
    @DisplayName("GET /api/admin/users - User should get 403 Forbidden")
    void getAllUsers_asUser_shouldBeForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/admin/users/{userId}/lock - Admin can lock a user")
    void lockUserAccount_asAdmin_shouldSucceed() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/admin/users/{userId}/lock", testUser.id())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    @DisplayName("POST /api/admin/cards - Admin can create a card for a user")
    void createCard_asAdmin_shouldSucceed() throws Exception {
        // Arrange
        CreateCardRequest cardReq = new CreateCardRequest(
                testUser.id(),
                "4444555566667777",
                YearMonth.now().plusYears(1),
                new BigDecimal("123.45")
        );

        // Act & Assert
        mockMvc.perform(post("/api/admin/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 7777"))
                .andExpect(jsonPath("$.balance").value(123.45));
    }

    @Test
    @DisplayName("DELETE /api/admin/cards/{cardId} - Admin can soft-delete a card")
    void softDeleteCard_asAdmin_shouldMakeCardInactive() throws Exception {
        // Arrange
        // Сначала создаем карту, чтобы было что удалять
        Card card = new Card();
        card.setOwner(user);
        card.setCardNumber("9999888877776666");
        card.setExpirationDate(YearMonth.now().plusYears(1));
        card.setActive(true);
        card.setStatus(CardStatus.ACTIVE);
        card = cardRepository.save(card);
        UUID cardId = card.getId();

        // Act & Assert
        mockMvc.perform(delete("/api/admin/cards/{cardId}", cardId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent()); // Ожидаем 204 No Content

        // Проверяем состояние в БД напрямую
        Card deletedCard = cardRepository.findById(cardId).orElseThrow();
        assertFalse(deletedCard.isActive(), "Card should be inactive after soft delete");
    }

    @Test
    @DisplayName("GET /api/admin/users/{userId} - Should return 404 for non-existent user")
    void findUserById_withInvalidId_shouldReturn404() throws Exception {
        // Arrange
        UUID nonExistentUserId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/{userId}", nonExistentUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // --- Helper Methods ---
    private String loginAndGetToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk()).andReturn();
        JwtResponse jwtResponse = objectMapper.readValue(result.getResponse().getContentAsString(), JwtResponse.class);
        return jwtResponse.accessToken();
    }


}
