package org.ganjp.blog.auth.service;

import org.ganjp.blog.auth.model.dto.request.LoginRequest;
import org.ganjp.blog.auth.model.entity.User;
import org.ganjp.blog.auth.model.enums.AccountStatus;
import org.ganjp.blog.auth.repository.RoleRepository;
import org.ganjp.blog.auth.repository.UserRepository;
import org.ganjp.blog.auth.repository.UserRoleRepository;
import org.ganjp.blog.auth.security.CustomAuthenticationProvider;
import org.ganjp.blog.auth.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * End-to-end test that verifies the complete authentication flow from AuthService
 * through CustomAuthenticationProvider, including login failure tracking.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceEndToEndTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private UserRoleRepository userRoleRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtils jwtUtils;
    
    private AuthService authService;
    private CustomAuthenticationProvider authenticationProvider;
    private AuthenticationManager authenticationManager;
    
    private User testUser;
    private static final String USERNAME = "testuser";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded.password.hash";
    private static final String JWT_TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        // Create real CustomAuthenticationProvider with mocked dependencies
        authenticationProvider = new CustomAuthenticationProvider(userRepository, passwordEncoder);
        
        // Create AuthenticationManager with our CustomAuthenticationProvider
        authenticationManager = new ProviderManager(List.of(authenticationProvider));
        
        // Create AuthService with real AuthenticationManager
        authService = new AuthService(
            authenticationManager,
            jwtUtils,
            userRepository,
            roleRepository,
            userRoleRepository,
            passwordEncoder
        );
        
        testUser = User.builder()
                .id("1")
                .username(USERNAME)
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .accountStatus(AccountStatus.active)
                .failedLoginAttempts(0)
                .active(true)
                .passwordChangedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("End-to-end successful login should work with failure counter reset")
    void endToEndSuccessfulLogin() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .build();
        
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtUtils.generateToken(testUser)).thenReturn(JWT_TOKEN);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        var response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(JWT_TOKEN, response.getToken());
        assertEquals(USERNAME, response.getUsername());
        
        // Verify user lookup was performed
        verify(userRepository).findByUsername(USERNAME);
        
        // Verify password was checked
        verify(passwordEncoder).matches(PASSWORD, ENCODED_PASSWORD);
        
        // Verify user was saved (for login tracking)
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertEquals(0, savedUser.getFailedLoginAttempts()); // Should reset to 0
        assertNotNull(savedUser.getLastLoginAt());
    }

    @Test
    @DisplayName("End-to-end failed login should update failure metrics in database")
    void endToEndFailedLoginShouldUpdateFailureMetrics() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username(USERNAME)
                .password("wrongpassword")
                .build();
        
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", ENCODED_PASSWORD)).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertEquals("Invalid password", exception.getMessage());
        
        // Verify the complete authentication flow occurred
        verify(userRepository).findByUsername(USERNAME);
        verify(passwordEncoder).matches("wrongpassword", ENCODED_PASSWORD);
        
        // Verify failure tracking was called
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        
        // Verify timestamp is recent
        LocalDateTime capturedTime = timeCaptor.getValue();
        LocalDateTime now = LocalDateTime.now();
        assertTrue(capturedTime.isAfter(now.minusMinutes(1)) && capturedTime.isBefore(now.plusMinutes(1)));
        
        // Verify JWT was never generated and user was never saved for success
        verify(jwtUtils, never()).generateToken(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("End-to-end failed login with non-existent user should attempt failure tracking")
    void endToEndFailedLoginWithNonExistentUser() {
        // Arrange
        String nonExistentUsername = "nonexistent";
        LoginRequest loginRequest = LoginRequest.builder()
                .username(nonExistentUsername)
                .password(PASSWORD)
                .build();
        
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertEquals("Invalid username", exception.getMessage());
        
        // Verify user lookup was attempted
        verify(userRepository).findByUsername(nonExistentUsername);
        
        // Verify password was never checked (user not found)
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("End-to-end email login should work with proper failure tracking")
    void endToEndEmailLoginWithFailureTracking() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email(EMAIL)
                .password("wrongpassword")
                .build();
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", ENCODED_PASSWORD)).thenReturn(false);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
        
        // Verify email lookup was used
        verify(userRepository).findByEmail(EMAIL);
        verify(userRepository, never()).findByUsername(any());
    }
}
