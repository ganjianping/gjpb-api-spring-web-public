package org.ganjp.blog.am.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.am.model.enums.AccountStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for user responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String username;
    private String nickname;
    private String email;
    private String mobileCountryCode;
    private String mobileNumber;
    private AccountStatus accountStatus;
    private Boolean active;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RoleResponse> roles;
}
