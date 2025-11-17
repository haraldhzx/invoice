package com.invoiceapp.service;

import com.invoiceapp.model.dto.AuthResponse;
import com.invoiceapp.model.dto.LoginRequest;
import com.invoiceapp.model.dto.RegisterRequest;
import com.invoiceapp.model.entity.RefreshToken;
import com.invoiceapp.model.entity.User;
import com.invoiceapp.model.entity.UserRole;
import com.invoiceapp.repository.RefreshTokenRepository;
import com.invoiceapp.repository.UserRepository;
import com.invoiceapp.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setRole(UserRole.USER);
        testUser.setCreatedAt(LocalDateTime.now());

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("Password123!");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password123!");
    }

    @Test
    void register_WithValidRequest_ShouldCreateUserAndReturnAuthResponse() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateAccessToken(anyString())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("refreshToken");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(anyString())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("refreshToken");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals(testUser.getEmail(), response.getUser().getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).save(testUser); // Save to update lastLoginAt
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowException() {
        // Arrange
        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("Bad credentials"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void logout_ShouldRevokeRefreshToken() {
        // Arrange
        String tokenValue = "refreshToken";
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(UUID.randomUUID());
        refreshToken.setToken(tokenValue);
        refreshToken.setUser(testUser);
        refreshToken.setRevoked(false);

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(refreshToken));

        // Act
        authService.logout(tokenValue);

        // Assert
        assertTrue(refreshToken.isRevoked());
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewTokens() {
        // Arrange
        String oldToken = "oldRefreshToken";
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(UUID.randomUUID());
        refreshToken.setToken(oldToken);
        refreshToken.setUser(testUser);
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        when(refreshTokenRepository.findByToken(oldToken)).thenReturn(Optional.of(refreshToken));
        when(jwtTokenProvider.generateAccessToken(anyString())).thenReturn("newAccessToken");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("newRefreshToken");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);

        // Act
        AuthResponse response = authService.refreshToken(oldToken);

        // Assert
        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("newRefreshToken", response.getRefreshToken());
        assertTrue(refreshToken.isRevoked()); // Old token should be revoked
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class)); // Revoke old, save new
    }

    @Test
    void refreshToken_WithExpiredToken_ShouldThrowException() {
        // Arrange
        String expiredToken = "expiredRefreshToken";
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(expiredToken);
        refreshToken.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired

        when(refreshTokenRepository.findByToken(expiredToken)).thenReturn(Optional.of(refreshToken));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.refreshToken(expiredToken));
    }

    @Test
    void refreshToken_WithRevokedToken_ShouldThrowException() {
        // Arrange
        String revokedToken = "revokedRefreshToken";
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(revokedToken);
        refreshToken.setRevoked(true);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        when(refreshTokenRepository.findByToken(revokedToken)).thenReturn(Optional.of(refreshToken));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.refreshToken(revokedToken));
    }
}
