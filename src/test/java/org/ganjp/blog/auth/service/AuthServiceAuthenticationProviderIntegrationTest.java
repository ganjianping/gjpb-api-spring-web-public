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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration test to verify that AuthService properly delegates authentication 
 * to CustomAuthenticationProvider and that login failure tracking works correctly.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceAuthenticationProviderIntegrationTest {

    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private JwtUtils jwtUtils;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private UserRoleRepository userRoleRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    private AuthService authService;
    
    private User testUser;
    private static final String USERNAME = "testuser";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String JWT_TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
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
                .password("encoded_password")
                .accountStatus(AccountStatus.active)
                .failedLoginAttempts(0)
                .active(true)
                .passwordChangedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Successful login should delegate to AuthenticationManager and return token")
    void successfulLoginShouldDelegateToAuthenticationManager() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .build();
        
        Authentication successfulAuth = new UsernamePasswordAuthenticationToken(
                testUser, null, testUser.getAuthorities());
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(successfulAuth);
        when(jwtUtils.generateToken(testUser)).thenReturn(JWT_TOKEN);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(JWT_TOKEN, response.getToken());
        assertEquals(USERNAME, response.getUsername());
        assertEquals(EMAIL, response.getEmail());
        
        // Verify AuthenticationManager was called with correct parameters
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor = 
            ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        
        UsernamePasswordAuthenticationToken capturedAuth = authCaptor.getValue();
        assertEquals(USERNAME, capturedAuth.getName());
        assertEquals(PASSWORD, capturedAuth.getCredentials());
        
        // Verify that the LoginRequest was set as details
        assertTrue(capturedAuth.getDetails() instanceof LoginRequest);
        LoginRequest capturedLoginRequest = (LoginRequest) capturedAuth.getDetails();
        assertEquals(USERNAME, capturedLoginRequest.getUsername());
        
        // Verify user was saved (for login tracking)
        verify(userRepository).save(testUser);
    }
    
    @Test
    @DisplayName("Failed login should delegate to AuthenticationManager and propagate exception")
    void failedLoginShouldDelegateToAuthenticationManager() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username(USERNAME)
                .password("wrongpassword")
                .build();
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        // Act & Assert
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertEquals("Invalid username or password", exception.getMessage());
        
        // Verify AuthenticationManager was called
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        
        // Verify no JWT token was generated or user saved
        verify(jwtUtils, never()).generateToken(any());
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Email login should use email as principal")
    void emailLoginShouldUseEmailAsPrincipal() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .build();
        
        Authentication successfulAuth = new UsernamePasswordAuthenticationToken(
                testUser, null, testUser.getAuthorities());
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(successfulAuth);
        when(jwtUtils.generateToken(testUser)).thenReturn(JWT_TOKEN);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.login(loginRequest);

        // Assert
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor = 
            ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        
        UsernamePasswordAuthenticationToken capturedAuth = authCaptor.getValue();
        assertEquals(EMAIL, capturedAuth.getName()); // Email should be the principal
        
        LoginRequest capturedLoginRequest = (LoginRequest) capturedAuth.getDetails();
        assertEquals(EMAIL, capturedLoginRequest.getEmail());
    }
    
    @Test
    @DisplayName("Mobile login should use country code and mobile number as principal")
    void mobileLoginShouldUseMobileNumberAsPrincipal() {
        // Arrange
        String mobileCountryCode = "+1";
        String mobileNumber = "1234567890";
        
        LoginRequest loginRequest = LoginRequest.builder()
                .mobileCountryCode(mobileCountryCode)
                .mobileNumber(mobileNumber)
                .password(PASSWORD)
                .build();
        
        Authentication successfulAuth = new UsernamePasswordAuthenticationToken(
                testUser, null, testUser.getAuthorities());
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(successfulAuth);
        when(jwtUtils.generateToken(testUser)).thenReturn(JWT_TOKEN);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.login(loginRequest);

        // Assert
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor = 
            ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        
        UsernamePasswordAuthenticationToken capturedAuth = authCaptor.getValue();
        assertEquals(mobileCountryCode + "-" + mobileNumber, capturedAuth.getName()); // Combined format should be the principal
        
        LoginRequest capturedLoginRequest = (LoginRequest) capturedAuth.getDetails();
        assertEquals(mobileCountryCode, capturedLoginRequest.getMobileCountryCode());
        assertEquals(mobileNumber, capturedLoginRequest.getMobileNumber());
    }

    @Test
    @DisplayName("Invalid login method should throw BadCredentialsException")
    void invalidLoginMethodShouldThrowException() {
        // Arrange - LoginRequest with multiple login methods (invalid)
        LoginRequest loginRequest = LoginRequest.builder()
                .username(USERNAME)
                .email(EMAIL) // Having both username and email is invalid
                .password(PASSWORD)
                .build();

        // Act & Assert
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
        
        assertEquals("Please provide exactly one login method: username, email, or mobile number", 
                     exception.getMessage());
        
        // Verify AuthenticationManager was never called
        verify(authenticationManager, never()).authenticate(any());
    }
}
