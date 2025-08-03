package com.example.bankcards.service;

import com.example.bankcards.dto.request.LoginRequest;
import com.example.bankcards.dto.request.RegistrationRequest;
import com.example.bankcards.dto.response.JwtResponse;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.DuplicateResourceException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("registerUser should save a new user with encoded password")
    void registerUser_shouldSaveNewUser_whenUsernameIsUnique() {
        // --- Arrange ---
        RegistrationRequest request = new RegistrationRequest("testuser", "password123");
        String encodedPassword = "encodedPassword123";

        // Настраиваем моки
        // 1. Проверка на существование возвращает false (юзер не найден)
        when(userRepository.existsByUsername(request.username())).thenReturn(false);

        // 2. Кодировщик паролей возвращает заранее определенную строку
        when(passwordEncoder.encode(request.password())).thenReturn(encodedPassword);

        // 3. Метод save репозитория просто возвращает тот объект User, который ему передали
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 4. Маппер возвращает мок DTO
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(mock(UserResponseDto.class));

        // --- Act ---
        authService.registerUser(request);

        // --- Assert ---

        // Создаем "захватчик" аргументов, чтобы проверить, с чем был вызван метод save
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        // Проверяем, что метод save был вызван ровно 1 раз
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());

        // Получаем пользователя, который был передан в save
        User savedUser = userArgumentCaptor.getValue();

        // Проверяем поля сохраненного пользователя
        assertNotNull(savedUser);
        assertEquals(request.username(), savedUser.getUsername());
        assertEquals(encodedPassword, savedUser.getPassword()); // Самая важная проверка!
        assertTrue(savedUser.getRoles().contains(com.example.bankcards.entity.enums.Role.ROLE_USER));
        assertTrue(savedUser.isEnabled());
    }

    @Test
    @DisplayName("registerUser should throw DuplicateResourceException when username exists")
    void registerUser_shouldThrowException_whenUsernameExists() {
        // --- Arrange ---
        RegistrationRequest request = new RegistrationRequest("testuser", "password123");

        // Настраиваем мок: проверка на существование возвращает true
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        // --- Act & Assert ---

        // Проверяем, что вызов метода приводит к выбросу  кастомного исключения
        assertThrows(DuplicateResourceException.class, () -> {
            authService.registerUser(request);
        });

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("loginUser should return JwtResponse on successful authentication")
    void loginUser_shouldReturnJwtResponse_onSuccessfulAuthentication() {
        // --- Arrange ---
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        // Создаем тестового пользователя, который якобы будет возвращен после аутентификации
        User authenticatedUser = new User();
        authenticatedUser.setId(UUID.randomUUID());
        authenticatedUser.setUsername(loginRequest.identifier());

        // Создаем мок объекта Authentication, который вернет AuthenticationManager
        Authentication authentication = mock(Authentication.class);

        // Создаем мок RefreshToken
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("test-refresh-token");

        // Настраиваем моки
        // 1. Когда authenticationManager.authenticate будет вызван с правильными данными,
        // он должен вернуть наш мок-объект authentication.
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class))
        ).thenReturn(authentication);

        // 2. Когда у authentication спросят "getPrincipal", он вернет пользователя.
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);

        // 3. Когда jwtService попросят сгенерировать токен, он вернет тестовую строку.
        when(jwtService.generateAccessToken(authentication)).thenReturn("test-access-token");

        // 4. Когда refreshTokenService попросят создать токен, он вернет наш мок-объект.
        when(refreshTokenService.createRefreshToken(authenticatedUser.getId())).thenReturn(refreshToken);

        // --- Act ---
        JwtResponse jwtResponse = authService.loginUser(loginRequest);

        // --- Assert ---
        assertNotNull(jwtResponse);
        assertEquals("test-access-token", jwtResponse.accessToken());
        assertEquals("test-refresh-token", jwtResponse.refreshToken());
        assertEquals(authenticatedUser.getId(), jwtResponse.id());
        assertEquals(authenticatedUser.getUsername(), jwtResponse.username());

        // Проверяем, что все ключевые методы были вызваны
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateAccessToken(authentication);
        verify(refreshTokenService, times(1)).createRefreshToken(authenticatedUser.getId());
    }
}
