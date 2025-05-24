package org.ganjp.blog.am.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.am.model.enums.AccountStatus;

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
    private String firstName;
    private String lastName;
    private AccountStatus accountStatus;
    private Boolean active; // Backward compatibility
    private String message;
}
