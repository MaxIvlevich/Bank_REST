package com.example.bankcards.controller;

import com.example.bankcards.AbstractIntegrationTest;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.JwtResponse;
import com.example.bankcards.dto.response.PagedResponse;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;


public class CardFlowIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом
        refreshTokenRepository.deleteAll();
        cardRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем админа для каждого теста
        User admin = new User();
        admin.setUsername("test-admin");
        admin.setPassword(passwordEncoder.encode("test-password"));
        admin.setRoles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
        admin.setEnabled(true);
        userRepository.save(admin);
    }

    // Helper-метод для логина и получения токена
    @Test
    @DisplayName("Full workflow: Admin manages user and cards, user performs transactions")
    void fullCardWorkflowTest() throws Exception {
        // === Шаг 1 & 2: Админ логинится и создается тестовый юзер ===
        // Мы используем админский логин из application-test.yml
        String adminToken = loginAndGetToken("test-admin", "test-password");
        UserResponseDto newUser = registerUser("carduser", "password123");
        UUID userId = newUser.id();

        // === Шаг 3: Админ создает две карты для пользователя с начальным балансом ===
        CreateCardRequest card1Req = new CreateCardRequest(userId, "1111000011110001", YearMonth.now().plusYears(2), new BigDecimal("1000.00"));
        CreateCardRequest card2Req = new CreateCardRequest(userId, "1111000011110002", YearMonth.now().plusYears(3), new BigDecimal("500.00"));

        CardResponse card1 = createCardAsAdmin(adminToken, card1Req);
        CardResponse card2 = createCardAsAdmin(adminToken, card2Req);
        UUID card1Id = card1.id();
        UUID card2Id = card2.id();

        // === Шаг 4: Пользователь заходит в систему ===
        String userToken = loginAndGetToken("carduser", "password123");

        // === Шаг 5: Пользователь проверяет свои карты и балансы ===
        MvcResult getCardsResult = mockMvc.perform(get("/api/cards/my")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn();

        PagedResponse<CardResponse> initialCards = objectMapper.readValue(
                getCardsResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        assertThat(initialCards.content()).hasSize(2);
        assertThat(initialCards.content()).extracting(CardResponse::balance)
                .containsExactlyInAnyOrder(new BigDecimal("1000.00"), new BigDecimal("500.00"));

        // === Шаг 6: Пользователь успешно переводит 200.50 ===
        TransferRequest transferRequest = new TransferRequest(card1Id, card2Id, new BigDecimal("200.50"));
        mockMvc.perform(post("/api/cards/my/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk());

        // === Шаг 7: Пользователь проверяет новые балансы ===
        getCardsResult = mockMvc.perform(get("/api/cards/my")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn();

        PagedResponse<CardResponse> updatedCards = objectMapper.readValue(
                getCardsResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        CardResponse updatedCard1 = findCardInList(updatedCards.content(), card1Id);
        CardResponse updatedCard2 = findCardInList(updatedCards.content(), card2Id);

        assertThat(updatedCard1.balance()).isEqualByComparingTo("799.50");
        assertThat(updatedCard2.balance()).isEqualByComparingTo("700.50");

        // === Шаг 8: Пользователь пытается перевести слишком много и получает ошибку ===
        TransferRequest failedRequest = new TransferRequest(card1Id, card2Id, new BigDecimal("800.00"));
        mockMvc.perform(post("/api/cards/my/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(failedRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient funds. Required: 800.00, Available: 799.50"));
    }

    // --- Helper Methods ---

    private String loginAndGetToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        JwtResponse jwtResponse = objectMapper.readValue(result.getResponse().getContentAsString(), JwtResponse.class);
        return jwtResponse.accessToken();
    }

    private UserResponseDto registerUser(String username, String password) throws Exception {
        RegistrationRequest req = new RegistrationRequest(username, password);
        MvcResult res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readValue(res.getResponse().getContentAsString(), UserResponseDto.class);
    }

    private CardResponse createCardAsAdmin(String adminToken, CreateCardRequest req) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/admin/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readValue(res.getResponse().getContentAsString(), CardResponse.class);
    }

    private CardResponse findCardInList(List<CardResponse> cards, UUID cardId) {
        return cards.stream()
                .filter(c -> c.id().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Card with id " + cardId + " not found in the response list"));
    }
}
