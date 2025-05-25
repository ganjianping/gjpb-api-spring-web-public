package org.ganjp.blog.am.security;

import org.ganjp.blog.am.model.dto.request.LoginRequest;
import org.ganjp.blog.am.model.entity.User;
import org.ganjp.blog.am.model.enums.AccountStatus;
import org.ganjp.blog.am.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationProviderTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomAuthenticationProvider authenticationProvider;

    private User testUser;
    private static final String USERNAME = "testuser";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"; // Encoded 'password123'

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("1")
                .username(USERNAME)
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .accountStatus(AccountStatus.active)
                .failedLoginAttempts(0)
                .active(true)
                .passwordChangedAt(LocalDateTime.now())  // Set password changed time to now
                .build();
    }

    @Test
    @DisplayName("Authentication with correct password should succeed")
    void authenticateWithCorrectPassword() {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // Act
        Authentication result = authenticationProvider.authenticate(authentication);

        // Assert
        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertEquals(testUser, result.getPrincipal());
    }

    @Test
    @DisplayName("Authentication with wrong password should throw exception and update failure metrics")
    void authenticateWithWrongPasswordShouldUpdateFailureMetrics() {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(USERNAME, "wrongpassword");
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", ENCODED_PASSWORD)).thenReturn(false);
        
        // Using lenient stubbing to avoid strict argument matching errors
        lenient().when(userRepository.updateLoginFailureById(
                eq(testUser.getId()), any(LocalDateTime.class))).thenReturn(1);

        // Act & Assert
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            authenticationProvider.authenticate(authentication);
        });

        // Verify exception message
        assertEquals("Invalid password", exception.getMessage());

        // Verify that updateLoginFailureById was called with correct parameters
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(userRepository, times(1)).updateLoginFailureById(
                eq(testUser.getId()),
                timeCaptor.capture()
        );

        // Verify time is recent
        LocalDateTime capturedTime = timeCaptor.getValue();
        LocalDateTime now = LocalDateTime.now();
        assertTrue(capturedTime.isAfter(now.minusMinutes(1)) && capturedTime.isBefore(now.plusMinutes(1)));
    }

    @Test
    @DisplayName("Authentication with non-existent user should throw exception and update failure metrics")
    void authenticateWithNonExistentUser() {
        // Arrange
        String nonExistentUsername = "nonexistentuser";
        Authentication authentication = new UsernamePasswordAuthenticationToken(nonExistentUsername, PASSWORD);
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            authenticationProvider.authenticate(authentication);
        });

        // Verify exception message
        assertEquals("Invalid username", exception.getMessage());
        
        // Verify that updateLoginFailureById was NOT called since user doesn't exist
        verify(userRepository, never()).updateLoginFailureById(
                any(), any()
        );
    }

    @Test
    @DisplayName("Authentication with email login request and wrong password should update failure metrics")
    void authenticateWithEmailLoginRequestAndWrongPassword() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email(EMAIL)
                .password("wrongpassword")
                .build();
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(EMAIL, "wrongpassword");
        ((UsernamePasswordAuthenticationToken)authentication).setDetails(loginRequest);
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", ENCODED_PASSWORD)).thenReturn(false);
        when(userRepository.updateLoginFailureById(eq(testUser.getId()), any(LocalDateTime.class))).thenReturn(1);

        // Act & Assert
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            authenticationProvider.authenticate(authentication);
        });

        // Verify exception message
        assertEquals("Invalid password", exception.getMessage());

        // Verify that updateLoginFailureById was called with correct parameters
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(userRepository, times(1)).updateLoginFailureById(
                eq(testUser.getId()),
                timeCaptor.capture()
        );
    }

    @Test
    @DisplayName("Supports method should return true for UsernamePasswordAuthenticationToken")
    void supportsMethodShouldReturnTrueForUsernamePasswordAuthenticationToken() {
        // Act
        boolean result = authenticationProvider.supports(UsernamePasswordAuthenticationToken.class);
        
        // Assert
        assertTrue(result);
    }
}
