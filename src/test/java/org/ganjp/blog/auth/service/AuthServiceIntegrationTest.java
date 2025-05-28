package org.ganjp.blog.auth.service;

import org.ganjp.blog.auth.model.dto.request.LoginRequest;
import org.ganjp.blog.auth.model.dto.response.LoginResponse;
import org.ganjp.blog.auth.model.entity.User;
import org.ganjp.blog.auth.model.enums.AccountStatus;
import org.ganjp.blog.auth.repository.RoleRepository;
import org.ganjp.blog.auth.repository.UserRepository;
import org.ganjp.blog.auth.repository.UserRoleRepository;
import org.ganjp.blog.auth.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Integration tests for AuthService using mocked dependencies.
 * These tests verify authentication flows with mocked user repository and security components.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(AuthServiceIntegrationTest.TestConfig.class)
public class AuthServiceIntegrationTest {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    private User testUser;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_TOKEN = "test.database.jwt.token";
    
    /**
     * Configuration class to provide mock beans for testing
     */
    @Configuration
    static class TestConfig {
        @Bean
        public UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }
        
        @Bean
        public RoleRepository roleRepository() {
            return Mockito.mock(RoleRepository.class);
        }
        
        @Bean
        public PasswordEncoder passwordEncoder() {
            return Mockito.mock(PasswordEncoder.class);
        }
        
        @Bean
        public UserDetailsService userDetailsService() {
            return Mockito.mock(UserDetailsService.class);
        }
        
        @Bean
        public UserRoleRepository userRoleRepository() {
            return Mockito.mock(UserRoleRepository.class);
        }
        
        @Bean
        public AuthenticationManager authenticationManager() {
            return Mockito.mock(AuthenticationManager.class);
        }
        
        @Bean
        public JwtUtils jwtUtils() {
            return Mockito.mock(JwtUtils.class);
        }
        
        @Bean
        public AuthService authService(AuthenticationManager authenticationManager, JwtUtils jwtUtils, 
                                      UserRepository userRepository, RoleRepository roleRepository, 
                                      UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder) {
            return new AuthService(authenticationManager, jwtUtils, userRepository, roleRepository, userRoleRepository, passwordEncoder);
        }
    }
    
    @BeforeEach
    public void setUp() {
        testUser = User.builder()
                .id("1")
                .username(TEST_USERNAME)
                .password(TEST_PASSWORD) // In a real app, this would be encoded
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .accountStatus(AccountStatus.active)
                .userRoles(new ArrayList<>()) // Initialize userRoles to avoid null pointer
                .build();
        
        // Set up authentication for valid credentials
        Authentication successfulAuth = new UsernamePasswordAuthenticationToken(
                testUser, null, testUser.getAuthorities());
        
        // Reset mocks to clear any previous interactions
        Mockito.reset(authenticationManager, jwtUtils);
        
        // Set up mock behaviors for specific test scenarios
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(TEST_USERNAME, TEST_PASSWORD)))
                .thenReturn(successfulAuth);
                
        when(jwtUtils.generateToken(any(UserDetails.class))).thenReturn(TEST_TOKEN);
    }
    
    @Test
    @DisplayName("Authentication with valid credentials should succeed")
    void databaseAuthWithValidCredentialsShouldSucceed() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username(TEST_USERNAME)
                .password(TEST_PASSWORD)
                .build();
            
        // Act
        LoginResponse response = authService.login(loginRequest);
            
        // Assert
        assertNotNull(response, "Login response should not be null");
        assertNotNull(response.getToken(), "JWT token should not be null");
        assertEquals(TEST_TOKEN, response.getToken(), "JWT token should match the expected value");
    }
    
    @Test
    @DisplayName("Authentication with invalid password should fail")
    void databaseAuthWithInvalidPasswordShouldFail() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username(TEST_USERNAME)
                .password("wrongPassword")
                .build();
        
        // Set up specific mock for this test case
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(TEST_USERNAME, "wrongPassword")))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
            
        // Act & Assert
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        }, "Should throw BadCredentialsException for invalid password");
            
        assertEquals("Invalid credentials", exception.getMessage(), "Exception message should match");
    }
    
    @Test
    @DisplayName("Authentication with non-existent user should fail")
    void databaseAuthWithNonExistentUserShouldFail() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username("nonExistentUser")
                .password("anyPassword")
                .build();
        
        // Set up specific mock for this test case
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("nonExistentUser", "anyPassword")))
                .thenThrow(new BadCredentialsException("User not found"));
        
        // Act & Assert
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        }, "Should throw BadCredentialsException for non-existent user");
        
        assertEquals("User not found", exception.getMessage(), "Exception message should match");
    }
}