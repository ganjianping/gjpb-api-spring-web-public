package org.ganjp.blog.bm.service;

import org.ganjp.blog.bm.model.dto.AppSettingResponse;
import org.ganjp.blog.bm.model.dto.CreateAppSettingRequest;
import org.ganjp.blog.bm.model.dto.UpdateAppSettingRequest;
import org.ganjp.blog.bm.model.entity.AppSetting;
import org.ganjp.blog.bm.repository.AppSettingRepository;
import org.ganjp.blog.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AppSettingService
 */
@ExtendWith(MockitoExtension.class)
class AppSettingServiceTest {

    @Mock
    private AppSettingRepository appSettingRepository;

    @InjectMocks
    private AppSettingService appSettingService;

    private AppSetting testSetting;
    private CreateAppSettingRequest createRequest;

    @BeforeEach
    void setUp() {
        testSetting = AppSetting.builder()
                .id("test-id")
                .name("app_name")
                .value("Test Application")
                .lang(AppSetting.Language.EN)
                .isSystem(false)
                .isPublic(true)
                .build();
        testSetting.setCreatedAt(LocalDateTime.now());
        testSetting.setUpdatedAt(LocalDateTime.now());
        testSetting.setCreatedBy("test-user");
        testSetting.setUpdatedBy("test-user");

        createRequest = CreateAppSettingRequest.builder()
                .name("app_name")
                .value("Test Application")
                .lang(AppSetting.Language.EN)
                .isSystem(false)
                .isPublic(true)
                .build();
    }

    @Test
    @DisplayName("Should get setting by ID successfully")
    void shouldGetSettingByIdSuccessfully() {
        // Given
        when(appSettingRepository.findById("test-id")).thenReturn(Optional.of(testSetting));

        // When
        AppSettingResponse result = appSettingService.getSettingById("test-id");

        // Then
        assertNotNull(result);
        assertEquals("test-id", result.getId());
        assertEquals("app_name", result.getName());
        assertEquals("Test Application", result.getValue());
        assertEquals(AppSetting.Language.EN, result.getLang());
        assertEquals(false, result.getIsSystem());
        assertEquals(true, result.getIsPublic());
    }

    @Test
    @DisplayName("Should throw exception when setting not found by ID")
    void shouldThrowExceptionWhenSettingNotFoundById() {
        // Given
        when(appSettingRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> appSettingService.getSettingById("non-existent"));
        
        assertTrue(exception.getMessage().contains("App setting not found with id"));
    }

    @Test
    @DisplayName("Should get setting by name and language successfully")
    void shouldGetSettingByNameAndLangSuccessfully() {
        // Given
        when(appSettingRepository.findByNameAndLang("app_name", AppSetting.Language.EN))
                .thenReturn(Optional.of(testSetting));

        // When
        AppSettingResponse result = appSettingService.getSettingByNameAndLang("app_name", AppSetting.Language.EN);

        // Then
        assertNotNull(result);
        assertEquals("app_name", result.getName());
        assertEquals(AppSetting.Language.EN, result.getLang());
    }

    @Test
    @DisplayName("Should get setting value successfully")
    void shouldGetSettingValueSuccessfully() {
        // Given
        when(appSettingRepository.findByNameAndLang("app_name", AppSetting.Language.EN))
                .thenReturn(Optional.of(testSetting));

        // When
        String result = appSettingService.getSettingValue("app_name", AppSetting.Language.EN);

        // Then
        assertEquals("Test Application", result);
    }

    @Test
    @DisplayName("Should return default value when setting not found")
    void shouldReturnDefaultValueWhenSettingNotFound() {
        // Given
        when(appSettingRepository.findByNameAndLang("non_existent", AppSetting.Language.EN))
                .thenReturn(Optional.empty());

        // When
        String result = appSettingService.getSettingValue("non_existent", AppSetting.Language.EN, "Default Value");

        // Then
        assertEquals("Default Value", result);
    }

    @Test
    @DisplayName("Should create setting successfully")
    void shouldCreateSettingSuccessfully() {
        // Given
        when(appSettingRepository.existsByNameAndLang("app_name", AppSetting.Language.EN))
                .thenReturn(false);
        when(appSettingRepository.save(any(AppSetting.class))).thenReturn(testSetting);

        // When
        AppSettingResponse result = appSettingService.createSetting(createRequest, "test-user");

        // Then
        assertNotNull(result);
        assertEquals("app_name", result.getName());
        assertEquals("Test Application", result.getValue());
        assertEquals(AppSetting.Language.EN, result.getLang());
        
        verify(appSettingRepository).save(any(AppSetting.class));
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate setting")
    void shouldThrowExceptionWhenCreatingDuplicateSetting() {
        // Given
        when(appSettingRepository.existsByNameAndLang("app_name", AppSetting.Language.EN))
                .thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> appSettingService.createSetting(createRequest, "test-user"));
        
        assertTrue(exception.getMessage().contains("App setting already exists"));
        verify(appSettingRepository, never()).save(any(AppSetting.class));
    }

    @Test
    @DisplayName("Should check if setting exists")
    void shouldCheckIfSettingExists() {
        // Given
        when(appSettingRepository.existsByNameAndLang("app_name", AppSetting.Language.EN))
                .thenReturn(true);

        // When
        boolean result = appSettingService.settingExists("app_name", AppSetting.Language.EN);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should throw exception when trying to delete system setting")
    void shouldThrowExceptionWhenDeletingSystemSetting() {
        // Given
        AppSetting systemSetting = AppSetting.builder()
                .id("system-id")
                .name("system_setting")
                .value("System Value")
                .lang(AppSetting.Language.EN)
                .isSystem(true)
                .isPublic(false)
                .build();

        when(appSettingRepository.findById("system-id")).thenReturn(Optional.of(systemSetting));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> appSettingService.deleteSetting("system-id", "test-user"));
        
        assertTrue(exception.getMessage().contains("System setting cannot be deleted"));
        verify(appSettingRepository, never()).delete(any(AppSetting.class));
    }

    @Test
    @DisplayName("Should update setting successfully")
    void shouldUpdateSettingSuccessfully() {
        // Given
        UpdateAppSettingRequest updateRequest = UpdateAppSettingRequest.builder()
                .name("updated_app_name")
                .value("Updated Test Application")
                .lang(AppSetting.Language.ZH)
                .isSystem(false)
                .isPublic(true)
                .build();

        AppSetting existingSetting = AppSetting.builder()
                .id("test-id")
                .name("app_name")
                .value("Test Application")
                .lang(AppSetting.Language.EN)
                .isSystem(false)
                .isPublic(false)
                .build();

        AppSetting updatedSetting = AppSetting.builder()
                .id("test-id")
                .name("updated_app_name")
                .value("Updated Test Application")
                .lang(AppSetting.Language.ZH)
                .isSystem(false)
                .isPublic(true)
                .build();

        when(appSettingRepository.findById("test-id")).thenReturn(Optional.of(existingSetting));
        when(appSettingRepository.findByNameAndLang("updated_app_name", AppSetting.Language.ZH))
                .thenReturn(Optional.empty());
        when(appSettingRepository.save(any(AppSetting.class))).thenReturn(updatedSetting);

        // When
        AppSettingResponse result = appSettingService.updateSetting("test-id", updateRequest, "test-user");

        // Then
        assertNotNull(result);
        assertEquals("updated_app_name", result.getName());
        assertEquals("Updated Test Application", result.getValue());
        assertEquals(AppSetting.Language.ZH, result.getLang());
        assertFalse(result.getIsSystem());
        assertTrue(result.getIsPublic());
        verify(appSettingRepository).save(any(AppSetting.class));
    }

    @Test
    @DisplayName("Should update only provided fields")
    void shouldUpdateOnlyProvidedFields() {
        // Given
        UpdateAppSettingRequest updateRequest = UpdateAppSettingRequest.builder()
                .value("Updated Value Only")
                .build();

        AppSetting existingSetting = AppSetting.builder()
                .id("test-id")
                .name("app_name")
                .value("Original Value")
                .lang(AppSetting.Language.EN)
                .isSystem(false)
                .isPublic(false)
                .build();

        when(appSettingRepository.findById("test-id")).thenReturn(Optional.of(existingSetting));
        when(appSettingRepository.save(any(AppSetting.class))).thenReturn(existingSetting);

        // When
        AppSettingResponse result = appSettingService.updateSetting("test-id", updateRequest, "test-user");

        // Then
        assertNotNull(result);
        assertEquals("app_name", result.getName()); // Should remain unchanged
        assertEquals("Updated Value Only", result.getValue()); // Should be updated
        assertEquals(AppSetting.Language.EN, result.getLang()); // Should remain unchanged
        verify(appSettingRepository).save(any(AppSetting.class));
    }

    @Test
    @DisplayName("Should throw exception when updating to duplicate name+lang combination")
    void shouldThrowExceptionWhenUpdatingToDuplicateNameLang() {
        // Given
        UpdateAppSettingRequest updateRequest = UpdateAppSettingRequest.builder()
                .name("existing_setting")
                .lang(AppSetting.Language.EN)
                .build();

        AppSetting existingSetting = AppSetting.builder()
                .id("test-id")
                .name("app_name")
                .value("Test Application")
                .lang(AppSetting.Language.ZH)
                .isSystem(false)
                .isPublic(false)
                .build();

        AppSetting duplicateSetting = AppSetting.builder()
                .id("other-id")
                .name("existing_setting")
                .value("Other Value")
                .lang(AppSetting.Language.EN)
                .isSystem(false)
                .isPublic(false)
                .build();

        when(appSettingRepository.findById("test-id")).thenReturn(Optional.of(existingSetting));
        when(appSettingRepository.findByNameAndLang("existing_setting", AppSetting.Language.EN))
                .thenReturn(Optional.of(duplicateSetting));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> appSettingService.updateSetting("test-id", updateRequest, "test-user"));
        
        assertTrue(exception.getMessage().contains("App setting already exists"));
        verify(appSettingRepository, never()).save(any(AppSetting.class));
    }

    @Test
    @DisplayName("Should prevent updating system setting unless isSystem is changed")
    void shouldPreventUpdatingSystemSettingUnlessIsSystemChanged() {
        // Given
        UpdateAppSettingRequest updateRequest = UpdateAppSettingRequest.builder()
                .value("New Value")
                .build();

        AppSetting systemSetting = AppSetting.builder()
                .id("system-id")
                .name("system_setting")
                .value("System Value")
                .lang(AppSetting.Language.EN)
                .isSystem(true)
                .isPublic(false)
                .build();

        when(appSettingRepository.findById("system-id")).thenReturn(Optional.of(systemSetting));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> appSettingService.updateSetting("system-id", updateRequest, "test-user"));
        
        assertTrue(exception.getMessage().contains("System setting cannot be modified"));
        verify(appSettingRepository, never()).save(any(AppSetting.class));
    }

    @Test
    @DisplayName("Should allow updating system setting when isSystem is changed")
    void shouldAllowUpdatingSystemSettingWhenIsSystemChanged() {
        // Given
        UpdateAppSettingRequest updateRequest = UpdateAppSettingRequest.builder()
                .value("New Value")
                .isSystem(false)
                .build();

        AppSetting systemSetting = AppSetting.builder()
                .id("system-id")
                .name("system_setting")
                .value("System Value")
                .lang(AppSetting.Language.EN)
                .isSystem(true)
                .isPublic(false)
                .build();

        when(appSettingRepository.findById("system-id")).thenReturn(Optional.of(systemSetting));
        when(appSettingRepository.save(any(AppSetting.class))).thenReturn(systemSetting);

        // When
        AppSettingResponse result = appSettingService.updateSetting("system-id", updateRequest, "test-user");

        // Then
        assertNotNull(result);
        verify(appSettingRepository).save(any(AppSetting.class));
    }
}
