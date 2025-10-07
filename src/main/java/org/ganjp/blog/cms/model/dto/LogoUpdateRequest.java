package org.ganjp.blog.cms.model.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO for updating a logo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoUpdateRequest {

    @Size(max = 255, message = "Logo name must not exceed 255 characters")
    private String name;

    @Pattern(regexp = "^https?://.*", message = "Original URL must be a valid HTTP/HTTPS URL")
    @Size(max = 500, message = "Original URL must not exceed 500 characters")
    private String originalUrl;

    private MultipartFile file;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    private org.ganjp.blog.cms.model.entity.Logo.Language lang;

    private Integer displayOrder;

    private Boolean isActive;

    /**
     * Check if there's a new image to update
     */
    public boolean hasNewImage() {
        return (file != null && !file.isEmpty()) || (originalUrl != null && !originalUrl.trim().isEmpty());
    }
}
