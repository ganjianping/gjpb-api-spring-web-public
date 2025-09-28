package org.ganjp.blog.auth.service;

import org.ganjp.blog.auth.model.dto.ChangePasswordRequest;
import org.ganjp.blog.auth.model.dto.UpdateProfileRequest;
import org.ganjp.blog.auth.model.dto.UserProfileResponse;
import org.ganjp.blog.auth.model.entity.User;
import org.ganjp.blog.auth.model.enums.AccountStatus;
import org.ganjp.blog.auth.repository.UserRepository;
import org.ganjp.blog.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileService Tests")
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserProfileService userProfileService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("test-user-id")
                .username("testuser")
                .nickname("Test User")
                .email("test@example.com")
                .mobileCountryCode("1")
                .mobileNumber("1234567890")
                .password("$2a$10$encoded.password")
                .accountStatus(AccountStatus.active)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should get current user profile successfully")
    void shouldGetCurrentUserProfileSuccessfully() {
        // Given
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));

        // When
        UserProfileResponse result = userProfileService.getCurrentUserProfile("test-user-id");

        // Then
        assertNotNull(result);
        assertEquals("test-user-id", result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("Test User", result.getNickname());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("1", result.getMobileCountryCode());
        assertEquals("1234567890", result.getMobileNumber());
        assertEquals(AccountStatus.active, result.getAccountStatus());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userProfileService.getCurrentUserProfile("invalid-id"));

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    @DisplayName("Should update profile successfully")
    void shouldUpdateProfileSuccessfully() {
        // Given
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .nickname("Updated Name")
                .email("updated@example.com")
                .mobileCountryCode("86")
                .mobileNumber("13800138000")
                .build();

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("updated@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByMobileCountryCodeAndMobileNumber("86", "13800138000"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserProfileResponse result = userProfileService.updateProfile("test-user-id", request);

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .email("existing@example.com")
                .build();

        User existingUser = User.builder()
                .id("another-user-id")
                .email("existing@example.com")
                .build();

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userProfileService.updateProfile("test-user-id", request));

        assertTrue(exception.getMessage().contains("Email is already in use"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when mobile number already exists")
    void shouldThrowExceptionWhenMobileNumberAlreadyExists() {
        // Given
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .mobileCountryCode("86")
                .mobileNumber("13800138000")
                .build();

        User existingUser = User.builder()
                .id("another-user-id")
                .mobileCountryCode("86")
                .mobileNumber("13800138000")
                .build();

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
        when(userRepository.findByMobileCountryCodeAndMobileNumber("86", "13800138000"))
                .thenReturn(Optional.of(existingUser));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userProfileService.updateProfile("test-user-id", request));

        assertTrue(exception.getMessage().contains("Mobile number is already in use"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        // Given
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("current123")
                .newPassword("newpassword123")
                .confirmPassword("newpassword123")
                .build();

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("current123", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("newpassword123", testUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("newpassword123")).thenReturn("$2a$10$new.encoded.password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userProfileService.changePassword("test-user-id", request);

        // Then
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("newpassword123");
    }

    @Test
    @DisplayName("Should throw exception when current password is incorrect")
    void shouldThrowExceptionWhenCurrentPasswordIncorrect() {
        // Given
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("wrongpassword")
                .newPassword("newpassword123")
                .confirmPassword("newpassword123")
                .build();

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userProfileService.changePassword("test-user-id", request));

        assertTrue(exception.getMessage().contains("Current password is incorrect"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when new password same as current")
    void shouldThrowExceptionWhenNewPasswordSameAsCurrent() {
        // Given
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("current123")
                .newPassword("current123")
                .confirmPassword("current123")
                .build();

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("current123", testUser.getPassword())).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userProfileService.changePassword("test-user-id", request));

        assertTrue(exception.getMessage().contains("New password must be different"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should check if email is taken correctly")
    void shouldCheckIfEmailIsTakenCorrectly() {
        // Given
        User existingUser = User.builder()
                .id("another-user-id")
                .email("taken@example.com")
                .build();

        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(existingUser));

        // When
        boolean isTaken = userProfileService.isEmailTaken("taken@example.com", "test-user-id");

        // Then
        assertTrue(isTaken);
    }

    @Test
    @DisplayName("Should allow email update for same user")
    void shouldAllowEmailUpdateForSameUser() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        boolean isTaken = userProfileService.isEmailTaken("test@example.com", "test-user-id");

        // Then
        assertFalse(isTaken);
    }
}