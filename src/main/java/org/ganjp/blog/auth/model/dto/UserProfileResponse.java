package org.ganjp.blog.auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.auth.model.enums.AccountStatus;

import java.time.LocalDateTime;

/**
 * DTO for user profile response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private String id;
    private String username;
    private String nickname;
    private String email;
    private String mobileCountryCode;
    private String mobileNumber;
    private AccountStatus accountStatus;
    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Get full mobile number (country code + number)
     */
    public String getFullMobileNumber() {
        if (mobileCountryCode != null && mobileNumber != null) {
            return "+" + mobileCountryCode + mobileNumber;
        }
        return null;
    }

    /**
     * Check if mobile number is provided
     */
    public boolean hasMobileNumber() {
        return mobileCountryCode != null && mobileNumber != null;
    }

    /**
     * Check if email is provided
     */
    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }
}