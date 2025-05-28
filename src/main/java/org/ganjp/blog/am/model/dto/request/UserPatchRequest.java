package org.ganjp.blog.am.model.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.am.model.enums.AccountStatus;

import java.util.Set;

/**
 * DTO for partial updates to an existing user (PATCH operation).
 * All fields are optional to support partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPatchRequest {
    
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Pattern(
        regexp = "^[A-Za-z0-9._-]{3,30}$",
        message = "Username must match the format: 3-30 characters, alphanumeric, dots, underscores, or hyphens"
    )
    private String username;
    
    @Size(max = 30, message = "Nickname must be at most 30 characters")
    private String nickname;
    
    @Email(message = "Email must be a valid email address")
    @Size(max = 128, message = "Email must be at most 128 characters")
    private String email;
    
    @Pattern(
        regexp = "^[1-9]\\d{0,3}$",
        message = "Mobile country code must be a valid number between 1-9999"
    )
    private String mobileCountryCode;
    
    @Pattern(
        regexp = "^\\d{4,15}$",
        message = "Mobile number must be between 4-15 digits"
    )
    private String mobileNumber;
    
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String password;
    
    private AccountStatus accountStatus;
    
    private Boolean active;
    
    private Set<String> roleCodes;
    
    // Ensure either both mobile fields are provided or neither
    @AssertTrue(message = "Both mobile country code and mobile number must be provided or neither")
    public boolean isMobileInfoValid() {
        if ((mobileCountryCode != null && mobileNumber == null) || 
            (mobileCountryCode == null && mobileNumber != null)) {
            return false;
        }
        return true;
    }
}
