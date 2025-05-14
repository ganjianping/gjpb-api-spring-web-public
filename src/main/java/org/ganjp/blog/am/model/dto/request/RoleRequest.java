package org.ganjp.blog.am.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {

    @NotBlank(message = "Role code is required")
    @Size(min = 3, max = 30, message = "Role code must be between 3 and 30 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Role code must contain only uppercase letters, numbers, and underscores")
    private String code;

    @NotBlank(message = "Role name is required")
    @Size(min = 3, max = 50, message = "Role name must be between 3 and 50 characters")
    private String name;

    private Integer displayOrder;
    
    private Boolean active;
}