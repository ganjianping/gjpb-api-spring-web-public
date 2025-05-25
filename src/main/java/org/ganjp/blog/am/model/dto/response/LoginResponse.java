package org.ganjp.blog.am.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.am.model.enums.AccountStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String email;
    private String mobileCountryCode;
    private String mobileNumber;
    private String nickname;
    private AccountStatus accountStatus;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime lastFailedLoginAt;
    private int failedLoginAttempts;
}