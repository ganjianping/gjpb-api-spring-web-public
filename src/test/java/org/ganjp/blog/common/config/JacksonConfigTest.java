package org.ganjp.blog.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ganjp.blog.auth.model.dto.response.LoginResponse;
import org.ganjp.blog.auth.model.enums.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify Jackson configuration for proper LocalDateTime serialization.
 */
@SpringBootTest
@ActiveProfiles("test")
class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("LocalDateTime fields should be serialized as ISO strings, not timestamp arrays")
    void localDateTimeShouldSerializeAsIsoString() throws Exception {
        // Arrange
        LocalDateTime testDateTime = LocalDateTime.of(2023, 12, 25, 14, 30, 45);
        
        LoginResponse response = LoginResponse.builder()
                .token("test.jwt.token")
                .username("testuser")
                .email("test@example.com")
                .accountStatus(AccountStatus.active)
                .lastLoginAt(testDateTime)
                .lastFailedLoginAt(testDateTime)
                .failedLoginAttempts(0)
                .build();

        // Act
        String json = objectMapper.writeValueAsString(response);

        // Assert
        // Should contain ISO string format, not array format
        assertTrue(json.contains("\"lastLoginAt\":\"2023-12-25T14:30:45\""), 
                "lastLoginAt should be serialized as ISO string");
        assertTrue(json.contains("\"lastFailedLoginAt\":\"2023-12-25T14:30:45\""), 
                "lastFailedLoginAt should be serialized as ISO string");
        
        // Should NOT contain array format (timestamp)
        assertFalse(json.contains("[2023,12,25,14,30,45]"), 
                "Should not contain timestamp array format");
        
        System.out.println("Serialized JSON: " + json);
    }

    @Test
    @DisplayName("Should be able to deserialize ISO date strings back to LocalDateTime")
    void shouldDeserializeIsoStringToLocalDateTime() throws Exception {
        // Arrange
        String json = "{\"token\":\"test.jwt.token\",\"username\":\"testuser\"," +
                "\"lastLoginAt\":\"2023-12-25T14:30:45\",\"lastFailedLoginAt\":\"2023-12-25T14:30:45\"," +
                "\"failedLoginAttempts\":0,\"roleCodes\":[]}";

        // Act
        LoginResponse response = objectMapper.readValue(json, LoginResponse.class);

        // Assert
        assertNotNull(response.getLastLoginAt());
        assertEquals(LocalDateTime.of(2023, 12, 25, 14, 30, 45), response.getLastLoginAt());
        assertEquals(LocalDateTime.of(2023, 12, 25, 14, 30, 45), response.getLastFailedLoginAt());
    }
}
