package org.ganjp.blog.auth.service;

import org.ganjp.blog.auth.model.dto.request.LoginRequest;
import org.ganjp.blog.auth.model.dto.response.LoginResponse;
import org.ganjp.blog.auth.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the AuthService class.
 * These tests use Mockito to mock dependencies and focus on testing the service's behavior in isolation.
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private JwtUtils jwtUtils;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private AuthService authService;
    
    private UserDetails userDetails;
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin123";
    private static final String TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        userDetails = User.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("Login with valid admin credentials should succeed")
    void loginWithValidAdminCredentialsShouldSucceed() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .build();
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateToken(userDetails)).thenReturn(TOKEN);

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(TOKEN, response.getToken());
    }

    @Test
    @DisplayName("Login with invalid credentials should throw BadCredentialsException")
    void loginWithInvalidCredentialsShouldThrowBadCredentialsException() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username(USERNAME)
                .password("wrongPassword")
                .build();
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Bad credentials", exception.getMessage());
    }

    @Test
    @DisplayName("Login with non-existent user should throw BadCredentialsException")
    void loginWithNonExistentUserShouldThrowBadCredentialsException() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username("nonExistentUser")
                .password("anyPassword")
                .build();
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Bad credentials", exception.getMessage());
    }
}