package org.ganjp.blog.auth.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.auth.model.enums.AccountStatus;

/**
 * DTO for user registration responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    private String id;
    private String username;
    private String email;
    private String mobileCountryCode;
    private String mobileNumber;
    private String nickname;
    private AccountStatus accountStatus;
    private Boolean active;
}
