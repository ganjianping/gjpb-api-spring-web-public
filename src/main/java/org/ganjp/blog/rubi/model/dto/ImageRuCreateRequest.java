package org.ganjp.blog.rubi.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.ImageRu.Language;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageRuCreateRequest {
   
    @NotBlank(message = "Image name is required")
    @Size(max = 255, message = "Image name must not exceed 255 characters")
    private String name;

    @Pattern(regexp = "^https?://.*", message = "Original URL must be a valid HTTP/HTTPS URL")
    @Size(max = 500, message = "Original URL must not exceed 500 characters")
    private String originalUrl;

    @Size(max = 255)
    private String sourceName;

    @Size(max = 255)
    private String filename;

    private MultipartFile file;

    @Size(max = 500)
    private String altText;

    private Integer term;
    private Integer week;

    @Size(max = 500)
    private String tags;

    @Builder.Default
    private org.ganjp.blog.rubi.model.entity.ImageRu.Language lang = org.ganjp.blog.rubi.model.entity.ImageRu.Language.EN;

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private Boolean isActive = true;

    public boolean hasImageSource() {
        return (file != null && !file.isEmpty()) || (originalUrl != null && !originalUrl.trim().isEmpty());
    }
}
