package org.ganjp.blog.am.service;

import org.ganjp.blog.am.model.dto.request.SignupRequest;
import org.ganjp.blog.am.model.dto.response.SignupResponse;
import org.ganjp.blog.am.model.entity.Role;
import org.ganjp.blog.am.model.entity.User;
import org.ganjp.blog.am.model.entity.UserRole;
import org.ganjp.blog.am.model.enums.AccountStatus;
import org.ganjp.blog.am.repository.RoleRepository;
import org.ganjp.blog.am.repository.UserRepository;
import org.ganjp.blog.am.repository.UserRoleRepository;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private UserRoleRepository userRoleRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    @DisplayName("Signup with valid data should create a new user")
    void signupWithValidDataShouldCreateNewUser() {
        // Arrange
        SignupRequest signupRequest = SignupRequest.builder()
                .username("testuser")
                .password("Password1!")
                .email("user@example.com")
                .build();
        
        Role userRole = Role.builder()
                .id("role-id")
                .code("USER")
                .build();
        
        ArrayList<UserRole> mockUserRoles = new ArrayList<>();
        
        UserRole mockUserRole = new UserRole();
        mockUserRole.setRole(userRole);
        mockUserRole.setActive(true);
        mockUserRoles.add(mockUserRole);
        
        User savedUser = User.builder()
                .id("generated-id")
                .username("testuser")
                .password("encoded-password")
                .accountStatus(AccountStatus.pending_verification)
                .userRoles(mockUserRoles)
                .build();
        
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(roleRepository.findByCode("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRoleRepository.save(any())).thenReturn(null); // We don't need the return value
        
        // Act
        SignupResponse response = authService.signup(signupRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals("generated-id", response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals(false, response.getActive()); // Should be false since account is pending verification
        
        // Verify interactions
        verify(userRepository).existsByUsername("testuser");
        verify(roleRepository).findByCode("USER");
        verify(passwordEncoder).encode("Password1!");
        
        // Capture the User object being saved to verify its properties
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertNotNull(capturedUser.getId()); // UUID was generated
        assertEquals("testuser", capturedUser.getUsername());
        assertEquals("encoded-password", capturedUser.getPassword());
        assertFalse(capturedUser.isActive()); // Should be false as account status is pending_verification
        assertEquals(AccountStatus.pending_verification, capturedUser.getAccountStatus());
    }
    
    @Test
    @DisplayName("Signup with existing username should throw IllegalArgumentException")
    void signupWithExistingUsernameShouldThrowException() {
        // Arrange
        SignupRequest signupRequest = SignupRequest.builder()
                .username("existinguser")
                .password("Password1!")
                .build();
        
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.signup(signupRequest)
        );
        
        assertEquals("Username is already taken", exception.getMessage());
        verify(userRepository).existsByUsername("existinguser");
        // Verify no further interactions happened
        verifyNoMoreInteractions(roleRepository, passwordEncoder, userRepository);
    }
    
    @Test
    @DisplayName("Signup when USER role not found should throw ResourceNotFoundException")
    void signupWhenUserRoleNotFoundShouldThrowException() {
        // Arrange
        SignupRequest signupRequest = SignupRequest.builder()
                .username("newuser")
                .password("Password1!")
                .build();
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByMobileCountryCodeAndMobileNumber(any(), any())).thenReturn(false);
        when(roleRepository.findByCode("USER")).thenReturn(Optional.empty());
        
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> authService.signup(signupRequest)
        );
        
        assertEquals("Role not found with code: USER", exception.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail(any());
        verify(userRepository).existsByMobileCountryCodeAndMobileNumber(any(), any());
        verify(roleRepository).findByCode("USER");
    }
    
    @Test
    @DisplayName("Test signup with new fields")
    void testSignupWithNewFields() {
        // Arrange
        SignupRequest signupRequest = SignupRequest.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("Password@123")
                .nickname("Tester")
                .mobileCountryCode("65")
                .mobileNumber("12345678")
                .build();

        Role userRole = new Role();
        userRole.setCode("USER");

        User savedUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser")
                .email("testuser@example.com")
                .nickname("Tester")
                .mobileCountryCode("65")
                .mobileNumber("12345678")
                .password("encoded-password")
                .accountStatus(AccountStatus.pending_verification)
                .active(false)
                .build();

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByMobileCountryCodeAndMobileNumber(anyString(), anyString())).thenReturn(false);
        when(roleRepository.findByCode("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        SignupResponse response = authService.signup(signupRequest);

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("testuser@example.com", response.getEmail());
        assertEquals("Tester", response.getNickname());
        assertEquals("65", response.getMobileCountryCode());
        assertEquals("12345678", response.getMobileNumber());
    }
}
